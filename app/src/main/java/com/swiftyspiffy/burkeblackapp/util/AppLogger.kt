package com.swiftyspiffy.burkeblackapp.util

import android.os.Build
import com.swiftyspiffy.burkeblackapp.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

object AppLogger {
    private data class LogEntry(val timestamp: Long, val message: String)

    private val entries = ConcurrentLinkedQueue<LogEntry>()
    private const val MAX_ENTRIES = 50
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    /**
     * Log a message. Always recorded in the diagnostic log.
     */
    fun log(message: String) {
        entries.add(LogEntry(System.currentTimeMillis(), message))
        while (entries.size > MAX_ENTRIES) {
            entries.poll()
        }
    }

    /**
     * Log a message that contains sensitive data (tokens, usernames, IPs).
     * Only recorded in DEBUG builds. In release builds, logs a redacted version.
     */
    fun logSensitive(tag: String, sensitiveMessage: String) {
        if (BuildConfig.DEBUG) {
            log("$tag: $sensitiveMessage")
        } else {
            log("$tag: [redacted]")
        }
    }

    fun getDiagnostics(username: String?): String {
        val dateFormat = SimpleDateFormat("M/d/yyyy, HH:mm", Locale.US)
        val sb = StringBuilder()

        sb.appendLine("=== App Diagnostics ===")
        sb.appendLine("Date: ${dateFormat.format(Date())}")
        sb.appendLine("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        sb.appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        sb.appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
        username?.let { sb.appendLine("Username: $it") }
        sb.appendLine("Log entries: ${entries.size}")
        sb.appendLine()
        sb.appendLine("=== Recent Events ===")

        entries.forEach { entry ->
            sb.appendLine("[${timeFormat.format(Date(entry.timestamp))}] ${entry.message}")
        }

        return sb.toString()
    }
}
