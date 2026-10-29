package com.swiftyspiffy.burkeblackapp.data.captains

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.api.BurkeBlackApi
import com.swiftyspiffy.burkeblackapp.data.api.SessionCompatibilityInterceptor
import com.swiftyspiffy.burkeblackapp.data.models.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit

class CaptainsTransportTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val captain =
        """{"id":"captain-one","name":"Captain","portrait":null,"votes":"7","rank":1,"future_field":true}"""

    @Test
    fun existingBareResponsesAndBothCountFormatsDecode() = runTest {
        val seen = mutableListOf<Request>()
        val client =
            CaptainsClient.httpClient()
                .newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    seen.add(request)
                    val body =
                        when (request.url.encodedPath.substringAfter("/api/v1/")) {
                            "captains/all",
                            "elections/results" -> "[$captain]"
                            "elections/schedule" ->
                                """{"start":"2026-10-28T07:45:57.598657355Z","end":"2026-11-01T22:00:00Z","now":"2026-10-29T06:00:00Z"}"""
                            "elections/currentWinner" -> "null"
                            else -> """[{"id":"123","name":"Viewer","votes":9,"rank":1}]"""
                        }
                    fixture(request, body)
                }
                .build()
        val source =
            CaptainsClient(CaptainsClient.createApi(client)) {
                fail("Public read attempted session renewal")
            }
        val election = source.election()
        val details = source.details()
        assertEquals(7L, election.candidates.single().votes)
        assertEquals("44338537:2026-10-28T07:45:57.598657355Z", election.schedule.key)
        assertNull(details.winner)
        assertEquals(9L, details.current.single().votes)
        assertEquals(6, seen.size)
        seen.forEach {
            assertEquals("overlays.burkeblack.tv", it.url.host)
            assertNull(it.header("Authorization"))
            assertNull(it.url.query)
        }
    }

    @Test
    fun privateRequestsRenewTheExistingMobileSessionAndKeepCredentialsOutOfUrls() = runTest {
        for (goAuth in listOf(false, true)) {
            val calls = mutableListOf<Request>()
            val appClient =
                OkHttpClient.Builder()
                    .addInterceptor(SessionCompatibilityInterceptor(goAuth))
                    .addInterceptor { chain ->
                        calls.add(chain.request())
                        fixture(chain.request(), """{"success":true,"data":{"renewed":true}}""")
                    }
                    .build()
            val app =
                Retrofit.Builder()
                    .baseUrl("https://api.burkeblack.tv/app/")
                    .client(appClient)
                    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                    .build()
                    .create(BurkeBlackApi::class.java)
            val captainClient =
                CaptainsClient.httpClient()
                    .newBuilder()
                    .addInterceptor { chain ->
                        calls.add(chain.request())
                        fixture(
                            chain.request(),
                            if (chain.request().method == "POST") """{"result":"success"}"""
                            else """{"balance":7}""",
                        )
                    }
                    .build()
            val source =
                CaptainsClient(CaptainsClient.createApi(captainClient)) { token ->
                    check(app.renewCaptainSession("Bearer $token").success)
                }
            val session = SessionIdentity("123", "synthetic-mobile-session")
            assertEquals(7L, source.balance(session))
            val vote = CaptainVoteRequest("captain-one", 2, "00000000-0000-0000-0000-000000000001")
            assertEquals("success", source.vote(session, vote).result)
            assertEquals(
                listOf(
                    "/app/auth/renew",
                    "/captain/api/v1/elections/123/tokenBalance",
                    "/app/auth/renew",
                    "/captain/api/v1/elections/vote",
                ),
                calls.map { it.url.encodedPath },
            )
            assertEquals("1", calls.first().header("X-Burke-Go-API"))
            calls.forEach {
                assertEquals("Bearer synthetic-mobile-session", it.header("Authorization"))
                assertNull(it.url.query)
            }
            val buffer = Buffer()
            calls.last().body!!.writeTo(buffer)
            assertEquals(vote, json.decodeFromString<CaptainVoteRequest>(buffer.readUtf8()))
            assertFalse(captainClient.retryOnConnectionFailure)
            assertFalse(captainClient.followRedirects)
        }
    }

    @Test
    fun failedRenewalDoesNotDispatchAVote() = runTest {
        var writes = 0
        val client =
            CaptainsClient.httpClient()
                .newBuilder()
                .addInterceptor { chain ->
                    writes++
                    fixture(chain.request(), """{"result":"success"}""")
                }
                .build()
        val source =
            CaptainsClient(CaptainsClient.createApi(client)) {
                throw CaptainsAuthenticationException()
            }
        try {
            source.vote(
                SessionIdentity("123", "synthetic"),
                CaptainVoteRequest("one", 1, "00000000-0000-0000-0000-000000000001"),
            )
            fail()
        } catch (_: CaptainsAuthenticationException) {}
        assertEquals(0, writes)
    }

    @Test
    fun onlyDefinitiveVoteRejectionsCanClearAReceipt() = runTest {
        for (status in listOf(400, 409, 401, 403, 503)) {
            var writes = 0
            val client =
                CaptainsClient.httpClient()
                    .newBuilder()
                    .addInterceptor { chain ->
                        writes++
                        fixture(chain.request(), "{}", status)
                    }
                    .build()
            val source = CaptainsClient(CaptainsClient.createApi(client)) {}
            try {
                val result =
                    source.vote(
                        viewer(),
                        CaptainVoteRequest("one", 1, "00000000-0000-0000-0000-000000000001"),
                    )
                assertTrue(status == 400 || status == 409)
                assertEquals("rejected", result.result)
            } catch (e: Exception) {
                if (status == 401 || status == 403) assertTrue(e is CaptainsAuthenticationException)
                else {
                    assertEquals(503, status)
                    assertTrue(e is retrofit2.HttpException)
                }
            }
            assertEquals(1, writes)
        }
    }

    private fun viewer() = SessionIdentity("123", "synthetic")

    @Test
    fun socketPacketsMustMatchTheChannelAndProtocol() {
        assertEquals(
            CaptainSocketEvent.REFRESH,
            captainSocketEvent(
                """{"version":1,"channel_id":"44338537","type":"kraken.captain.election_change"}"""
            ),
        )
        assertEquals(
            CaptainSocketEvent.CONNECTED,
            captainSocketEvent(
                """{"version":1,"channel_id":"44338537","type":"kraken.captain.overlay_instance_status"}"""
            ),
        )
        for (packet in
            listOf(
                "{",
                "null",
                """{"version":2,"channel_id":"44338537","type":"kraken.captain.vote_change"}""",
                """{"version":1,"channel_id":"40876073","type":"kraken.captain.vote_change"}""",
                """{"version":1,"channel_id":"44338537","type":"newraffle"}""",
            )) assertNull(captainSocketEvent(packet))
        for (value in listOf("-1", "1.5", "\"not-a-number\"")) {
            assertThrows(Exception::class.java) {
                json.decodeFromString<CaptainBalance>("{\"balance\":$value}")
            }
        }
    }

    private fun fixture(request: Request, body: String, status: Int = 200) =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(status)
            .message("Fixture")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
}
