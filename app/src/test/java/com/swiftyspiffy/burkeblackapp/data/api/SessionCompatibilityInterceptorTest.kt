package com.swiftyspiffy.burkeblackapp.data.api

import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class SessionCompatibilityInterceptorTest {
    private fun requests(enabled: Boolean, url: String, renewalStatus: Int = 200): Pair<List<Request>,Int> {
        val seen = mutableListOf<Request>()
        val client = OkHttpClient.Builder().addInterceptor(SessionCompatibilityInterceptor(enabled))
            .addInterceptor { chain ->
                val req = chain.request(); seen.add(req)
                val status = if (req.url.encodedPath == "/app/auth/renew") renewalStatus else 200
                Response.Builder().request(req).protocol(Protocol.HTTP_1_1).code(status).message("Fixture")
                    .body("{}".toResponseBody()).build()
            }.build()
        val request = Request.Builder().url(url).header("Authorization","Bearer fixture").get().build()
        val code = client.newCall(request).execute().use { it.code }
        return seen to code
    }
    @Test fun refreshUsesRenewalThenPost() {
        val (seen, code) = requests(true,"https://api.burkeblack.tv/app/twitch-token?unused=1")
        assertEquals(200,code);assertEquals(2,seen.size)
        assertEquals("/app/auth/renew",seen[0].url.encodedPath);assertNull(seen[0].url.query)
        assertEquals("POST",seen[0].method);assertEquals("POST",seen[1].method)
        seen.forEach { assertEquals("1", it.header("X-Burke-Go-API")) }
    }
    @Test fun failuresDoNotFallBack() {
        for (status in listOf(401,500,503)) { val (seen,code)=requests(true,"https://api.burkeblack.tv/app/dashboard",status);assertEquals(status,code);assertEquals(1,seen.size) }
    }
    @Test fun releaseAndOtherHostsAreUnchanged() {
        for ((enabled,url) in listOf(false to "https://api.burkeblack.tv/app/twitch-token",true to "https://api.twitch.tv/helix/users",true to "https://example.test/app/twitch-token",true to "http://api.burkeblack.tv/app/twitch-token",true to "https://api.burkeblack.tv:444/app/twitch-token")) {
            val (seen,_)=requests(enabled,url);assertEquals(1,seen.size);assertEquals("GET",seen[0].method);assertEquals(url,seen[0].url.toString())
            assertNull(seen[0].header("X-Burke-Go-API"))
        }
    }
}
