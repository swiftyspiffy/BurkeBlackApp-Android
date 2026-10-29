package com.swiftyspiffy.burkeblackapp.data.captains

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.data.models.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.decodeFromJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.*

interface CaptainsApi {
    @GET("captains/all") suspend fun crew(): List<Captain>

    @GET("elections/results") suspend fun candidates(): List<Captain>

    @GET("elections/schedule") suspend fun schedule(): CaptainSchedule

    @GET("elections/currentWinner") suspend fun winner(): JsonElement

    @GET("elections/leaderboard/current") suspend fun currentRanking(): List<CaptainRanking>

    @GET("elections/leaderboard/past") suspend fun lifetimeRanking(): List<CaptainRanking>

    @GET("elections/{userId}/tokenBalance")
    suspend fun balance(
        @Path("userId") userId: String,
        @Header("Authorization") auth: String,
    ): CaptainBalance

    @POST("elections/vote")
    suspend fun vote(
        @Header("Authorization") auth: String,
        @Body request: CaptainVoteRequest,
    ): CaptainVoteResult
}

class CaptainsAuthenticationException : Exception("Sign in with Twitch to use your voting tokens.")

interface CaptainsDataSource {
    suspend fun election(): CaptainElection

    suspend fun details(): CaptainDetails

    suspend fun renew(session: SessionIdentity)

    suspend fun balance(session: SessionIdentity): Long

    suspend fun vote(session: SessionIdentity, request: CaptainVoteRequest): CaptainVoteResult
}

class CaptainsClient(
    private val api: CaptainsApi,
    private val renewSession: suspend (String) -> Unit,
) : CaptainsDataSource {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun httpClient() =
            OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .followRedirects(false)
                .followSslRedirects(false)
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .callTimeout(15, TimeUnit.SECONDS)
                .build()

        fun createApi(
            client: OkHttpClient = httpClient(),
            baseUrl: String = CAPTAINS_BASE + "api/v1/",
        ): CaptainsApi =
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(CaptainsApi::class.java)

        fun live() =
            CaptainsClient(createApi()) { token ->
                val response = ApiClient.api.renewCaptainSession("Bearer $token")
                if (!response.success) throw CaptainsAuthenticationException()
            }
    }

    override suspend fun election(): CaptainElection = coroutineScope {
        val candidates = async { api.candidates() }
        val schedule = async { api.schedule().also { it.validate() } }
        CaptainElection(candidates.await(), schedule.await())
    }

    override suspend fun details(): CaptainDetails = coroutineScope {
        val crew = async { api.crew() }
        val winner = async {
            api.winner()
                .takeUnless { it == JsonNull }
                ?.let { json.decodeFromJsonElement<Captain>(it) }
        }
        val current = async { api.currentRanking() }
        val lifetime = async { api.lifetimeRanking() }
        CaptainDetails(crew.await(), winner.await(), current.await(), lifetime.await())
    }

    override suspend fun renew(session: SessionIdentity) {
        if (!session.hasUserId) throw CaptainsAuthenticationException()
        authorized { renewSession(session.token) }
    }

    override suspend fun balance(session: SessionIdentity): Long {
        renew(session)
        return authorized { api.balance(session.userId, "Bearer ${session.token}").balance }
    }

    override suspend fun vote(
        session: SessionIdentity,
        request: CaptainVoteRequest,
    ): CaptainVoteResult {
        request.validate()
        renew(session)
        return try {
            authorized { api.vote("Bearer ${session.token}", request) }
        } catch (e: HttpException) {
            // These responses confirm no vote was accepted for this payload. All other
            // failures retain the persisted request for an explicit idempotent check.
            if (e.code() == 400 || e.code() == 409) CaptainVoteResult("rejected") else throw e
        }
    }

    private suspend fun <T> authorized(block: suspend () -> T): T =
        try {
            block()
        } catch (e: HttpException) {
            if (e.code() == 401 || e.code() == 403) throw CaptainsAuthenticationException()
            throw e
        }
}
