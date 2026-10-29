package com.swiftyspiffy.burkeblackapp.data.models

import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class StreamDeckTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun decodesTheSharedIosApiContract() {
        val response =
            json.decodeFromString<ApiResponse<StreamDeckData>>(
                """
            {"success":true,"api_version":1,"data":{"fetched_at":"2026-10-29T05:00:00Z","is_stale":true,
            "channels":[{"key":"classics","login":"burkeblack247","display_name":"BurkeBlack247",
            "twitch_url":"https://www.twitch.tv/burkeblack247","is_live":true,"title":"",
            "game_name":"Always On","viewer_count":9,"thumbnail_url":"https://example.com/preview.jpg",
            "profile_image_url":null,"started_at":"2026-10-27T18:10:24Z"}]}}
        """
                    .trimIndent()
            )
        assertTrue(response.success)
        val deck = requireNotNull(response.data)
        assertTrue(deck.isStale)
        val classics = deck.orderedChannels().last()
        assertTrue(classics.isLive)
        assertEquals(9, classics.viewerCount)
        assertEquals("Classic BurkeBlack", classics.heroTitle)
        assertEquals("Watch 24/7 Classics", classics.watchButtonTitle)
        assertNull(classics.profileImageUrl)
    }

    @Test
    fun missingChannelsGetTheirCorrectFallbacksInStableOrder() {
        val placeholders = StreamDeckChannel.placeholders()
        val main = placeholders.first().copy(isLive = true, title = "Today's stream")
        val unknown = main.copy(key = "future-channel")
        val channels =
            StreamDeckData("now", channels = listOf(unknown, placeholders.last(), main))
                .orderedChannels()
        assertEquals(listOf("main", "burke40k", "classics"), channels.map { it.key })
        assertEquals(main, channels.first())
        assertEquals("https://www.twitch.tv/burke40k", channels[1].twitchUrl)
        assertFalse(channels[1].isLive)
        assertEquals("Today's stream", channels.first().heroTitle)
    }

    @Test
    fun blankStreamTitlesFallBackToTheChannelName() {
        val channel = StreamDeckChannel.placeholders()[1].copy(isLive = true, title = " ")
        assertEquals("Burke40k is live", channel.heroTitle)
    }
}
