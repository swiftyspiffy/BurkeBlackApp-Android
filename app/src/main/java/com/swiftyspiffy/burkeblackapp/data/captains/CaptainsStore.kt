package com.swiftyspiffy.burkeblackapp.data.captains

import com.swiftyspiffy.burkeblackapp.data.models.CaptainVoteRequest

interface CaptainsStore {
    suspend fun lastCaptainElection(): String?

    suspend fun markCaptainElection(key: String)

    suspend fun pendingCaptainVote(userId: String): CaptainVoteRequest?

    suspend fun saveCaptainVote(userId: String, request: CaptainVoteRequest?)
}
