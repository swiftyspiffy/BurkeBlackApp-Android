package com.swiftyspiffy.burkeblackapp.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamDeckData(
    @SerialName("fetched_at") val fetchedAt: String,
    @SerialName("is_stale") val isStale: Boolean = false,
    val channels: List<StreamDeckChannel>,
)

@Serializable
data class StreamDeckChannel(
    val key: String,
    val login: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("twitch_url") val twitchUrl: String,
    @SerialName("is_live") val isLive: Boolean = false,
    val title: String? = null,
    @SerialName("game_name") val gameName: String? = null,
    @SerialName("viewer_count") val viewerCount: Int? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    @SerialName("profile_image_url") val profileImageUrl: String? = null,
    @SerialName("started_at") val startedAt: String? = null,
) {
    val cardName: String
        get() =
            when (key) {
                "main" -> "BurkeBlack"
                "burke40k" -> "Burke40k"
                "classics" -> "24/7 Classics"
                else -> displayName
            }

    val heroTitle: String
        get() =
            if (key == "classics") "Classic BurkeBlack"
            else title?.takeIf { it.isNotBlank() } ?: "$displayName is live"

    val watchButtonTitle: String
        get() = "Watch $cardName"

    companion object {
        fun placeholders() =
            listOf(
                placeholder("main", "burkeblack", "BurkeBlack"),
                placeholder("burke40k", "burke40k", "Burke40k"),
                placeholder("classics", "burkeblack247", "BurkeBlack247"),
            )

        private fun placeholder(key: String, login: String, name: String) =
            StreamDeckChannel(key, login, name, "https://www.twitch.tv/$login")
    }
}

/** Keep the deck ordered and usable when an upstream response omits a channel. */
fun StreamDeckData.orderedChannels(): List<StreamDeckChannel> {
    val byKey = channels.associateBy { it.key }
    return StreamDeckChannel.placeholders().map { byKey[it.key] ?: it }
}
