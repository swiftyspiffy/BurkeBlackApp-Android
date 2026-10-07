package com.swiftyspiffy.burkeblackapp.auth

import java.security.MessageDigest
import java.security.SecureRandom

/** Pure state contract shared by browser callbacks and tests. */
internal object OAuthAttempt {
    fun nonce(): String = ByteArray(32).also { SecureRandom().nextBytes(it) }
        .joinToString("") { "%02x".format(it.toInt() and 0xff) }

    fun matches(expected: String?, received: List<String>, started: Long, now: Long): Boolean {
        if (expected == null || !expected.matches(Regex("[0-9a-f]{64}")) || received.size != 1) return false
        if (now < started || now - started >= 10 * 60 * 1000L) return false
        return MessageDigest.isEqual(expected.toByteArray(Charsets.UTF_8), received[0].toByteArray(Charsets.UTF_8))
    }
}
