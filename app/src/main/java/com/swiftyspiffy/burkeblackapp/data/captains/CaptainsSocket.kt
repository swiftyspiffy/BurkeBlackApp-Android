package com.swiftyspiffy.burkeblackapp.data.captains

import com.swiftyspiffy.burkeblackapp.auth.SessionIdentity
import com.swiftyspiffy.burkeblackapp.data.models.CAPTAINS_BASE
import com.swiftyspiffy.burkeblackapp.data.models.CAPTAINS_CHANNEL
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.*
import okhttp3.*

enum class CaptainSocketEvent {
    CONNECTED,
    REFRESH,
}

fun captainSocketEvent(text: String): CaptainSocketEvent? =
    runCatching {
            val packet = Json.parseToJsonElement(text).jsonObject
            if (
                packet["version"]?.jsonPrimitive?.intOrNull != 1 ||
                    packet["channel_id"]?.jsonPrimitive?.content != CAPTAINS_CHANNEL
            )
                return null
            val type = packet["type"]?.jsonPrimitive?.content ?: return null
            if (!type.startsWith("kraken.captain.")) return null
            if (
                type in
                    setOf(
                        "kraken.captain.token_change",
                        "kraken.captain.vote_change",
                        "kraken.captain.election_change",
                        "kraken.captain.captain_change",
                    )
            )
                CaptainSocketEvent.REFRESH
            else CaptainSocketEvent.CONNECTED
        }
        .getOrNull()

fun interface CaptainsSocket {
    fun events(session: SessionIdentity?): Flow<CaptainSocketEvent>
}

class LiveCaptainsSocket : CaptainsSocket {
    private val client = OkHttpClient.Builder().pingInterval(30, TimeUnit.SECONDS).build()

    override fun events(session: SessionIdentity?): Flow<CaptainSocketEvent> = callbackFlow {
        val socket =
            client.newWebSocket(
                Request.Builder()
                    .url(
                        CAPTAINS_BASE.replace("https:", "wss:") +
                            "ws/" +
                            if (session == null) "overlays" else "users"
                    )
                    .build(),
                object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        webSocket.send(
                            buildJsonObject {
                                    put("type", "IDENTITY")
                                    if (session != null) put("token", session.token)
                                    putJsonObject("data") {
                                        put(
                                            "identity",
                                            session?.userId ?: UUID.randomUUID().toString(),
                                        )
                                        putJsonObject("categories") {
                                            put("type", "mobile")
                                            put("platform", "android")
                                        }
                                    }
                                }
                                .toString()
                        )
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        captainSocketEvent(text)?.let { trySend(it) }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?,
                    ) {
                        if (response?.code in listOf(401, 403))
                            close(CaptainsAuthenticationException())
                        else close()
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        close()
                    }
                },
            )
        awaitClose { socket.cancel() }
    }
}
