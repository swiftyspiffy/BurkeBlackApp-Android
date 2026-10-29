package com.swiftyspiffy.burkeblackapp.data.models

import java.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

const val CAPTAINS_CHANNEL = "44338537"
const val CAPTAINS_BASE = "https://overlays.burkeblack.tv/captain/"
const val MAX_CAPTAIN_VOTES = 1_000_000L

/** The existing results API uses string counts, while leaderboards use JSON numbers. */
object CaptainCountSerializer : KSerializer<Long> {
    override val descriptor = PrimitiveSerialDescriptor("CaptainCount", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        val value =
            if (decoder is JsonDecoder) decoder.decodeJsonElement().jsonPrimitive.longOrNull
            else decoder.decodeLong()
        return value?.takeIf { it >= 0 } ?: throw SerializationException("Invalid captain count")
    }

    override fun serialize(encoder: Encoder, value: Long) = encoder.encodeLong(value)
}

@Serializable
data class Captain(
    val id: String,
    val name: String,
    val title: String = "",
    val lore: String = "",
    val portrait: String? = null,
    val order: Int = 0,
    val color: String? = null,
    val accent: String? = null,
    @Serializable(with = CaptainCountSerializer::class) val votes: Long = 0,
    val rank: Int = 0,
    val election: String? = null,
    val end: String? = null,
)

@Serializable
data class CaptainSchedule(val start: String, val end: String, val now: String) {
    val key: String
        get() = "$CAPTAINS_CHANNEL:${Instant.parse(start)}"

    fun validate() {
        require(Instant.parse(end) > Instant.parse(start))
        Instant.parse(now)
    }
}

@Serializable
data class CaptainRanking(
    val id: String,
    val name: String? = null,
    @Serializable(with = CaptainCountSerializer::class) val votes: Long = 0,
    val rank: Int = 0,
)

@Serializable
data class CaptainBalance(@Serializable(with = CaptainCountSerializer::class) val balance: Long)

@Serializable
data class CaptainVoteRequest(val captain: String, val amount: Long, val request_id: String) {
    fun validate() {
        require(
            captain.isNotBlank() &&
                amount in 1..MAX_CAPTAIN_VOTES &&
                request_id.matches(Regex("[a-zA-Z0-9-]{16,64}"))
        )
    }
}

@Serializable data class CaptainVoteResult(val result: String)

data class CaptainElection(val candidates: List<Captain>, val schedule: CaptainSchedule)

data class CaptainDetails(
    val crew: List<Captain>,
    val winner: Captain?,
    val current: List<CaptainRanking>,
    val lifetime: List<CaptainRanking>,
)
