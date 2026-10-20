package com.swiftyspiffy.burkeblackapp.data.api

import com.swiftyspiffy.burkeblackapp.data.models.GiveawayRuntimeOptions
import org.junit.Assert.*
import org.junit.Test

class GiveawayEntryFieldsTest {
    private val native = GiveawayRuntimeOptions("idle", listOf("none", "doubloons", "subscriber", "non_subscriber", "follower"), 5000)

    @Test fun durationsAndMinimumMatchTheBackend() {
        val legacy = giveawayEntryFields("none", "9", "300", "120", "1", null)
        assertEquals("5", legacy["entry_duration"])
        assertEquals("2", legacy["claim_duration"])
        assertFalse(legacy.containsKey("minimum_entries"))
        val current = giveawayEntryFields("follower", "9", "300", "120", "5", native)
        assertEquals("300", current["entry_duration"])
        assertEquals("120", current["claim_duration"])
        assertEquals("5", current["minimum_entries"])
        assertEquals("0", current["filter_amount"])
        val olderRuntime = giveawayEntryFields("none", "0", "300", "120", "1", GiveawayRuntimeOptions("idle"))
        assertEquals("300", olderRuntime["entry_duration"])
        assertFalse(olderRuntime.containsKey("minimum_entries"))
    }

    @Test fun unsupportedRulesAndInvalidTimingAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { giveawayEntryFields("subscriber", "0", "300", "120", "1", null) }
        assertThrows(IllegalArgumentException::class.java) { giveawayEntryFields("none", "0", "300", "120", "2", null) }
        for (entry in listOf("0", "119", "121", "86460", "bad")) {
            assertThrows(IllegalArgumentException::class.java) { giveawayEntryFields("none", "0", entry, "120", "1", native) }
        }
        for (minimum in listOf("0", "5001", "bad")) {
            assertThrows(IllegalArgumentException::class.java) { giveawayEntryFields("none", "0", "300", "120", minimum, native) }
        }
    }
}
