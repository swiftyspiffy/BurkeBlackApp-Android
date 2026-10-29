package com.swiftyspiffy.burkeblackapp.auth

/** Atomic identity from the existing session store. Never include credentials in diagnostics. */
data class SessionIdentity(val userId: String, val token: String) {
    val hasUserId: Boolean
        get() = userId.isNotEmpty() && userId.all { it in '0'..'9' }

    override fun toString() = "SessionIdentity([redacted])"
}
