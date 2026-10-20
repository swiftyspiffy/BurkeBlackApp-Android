package com.swiftyspiffy.burkeblackapp.data.api

import com.swiftyspiffy.burkeblackapp.data.models.GiveawayRuntimeOptions

// The older backend expects minutes; the native runtime accepts seconds.
fun giveawayEntryFields(filter: String, amount: String, entry: String, claim: String,
                        minimum: String, runtime: GiveawayRuntimeOptions?): Map<String, String> {
    val entrySeconds = entry.toIntOrNull()
    val claimSeconds = claim.toIntOrNull()
    val minEntries = minimum.toIntOrNull()
    require(entrySeconds != null && entrySeconds in 120..86400 && entrySeconds % 60 == 0 &&
        claimSeconds != null && claimSeconds in 60..86400 && claimSeconds % 60 == 0) {
        "Use whole-minute durations in seconds: at least 120 for entry and 60 for claim."
    }
    require(minEntries != null && minEntries in 1..(runtime?.maximumEntries ?: 1)) { "Invalid minimum entries." }
    require(filter in (runtime?.supportedFilters ?: listOf("none", "doubloons"))) { "This entry filter is unavailable." }
    require(filter != "doubloons" || (amount.toLongOrNull() ?: -1) in 0..999999999) { "Invalid Doubloons amount." }
    return buildMap {
        put("filter", filter)
        put("filter_amount", if (filter == "doubloons") amount else "0")
        put("entry_duration", (if (runtime == null) entrySeconds / 60 else entrySeconds).toString())
        put("claim_duration", (if (runtime == null) claimSeconds / 60 else claimSeconds).toString())
        if (runtime?.minimumEntriesMax != null) put("minimum_entries", minEntries.toString())
    }
}
