package com.swiftyspiffy.burkeblackapp.push

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.swiftyspiffy.burkeblackapp.auth.SessionManager
import com.swiftyspiffy.burkeblackapp.util.AppLogger

class PushTokenRegistrationWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val fcmToken = inputData.getString(KEY_FCM_TOKEN)
        if (fcmToken.isNullOrBlank()) {
            return Result.failure()
        }

        val bearerToken = SessionManager(applicationContext).getToken()
        if (bearerToken.isNullOrBlank()) {
            AppLogger.log("Push: token registration deferred until login")
            return Result.success()
        }

        if (PushNotificationManager.registerWithBackend(bearerToken, fcmToken)) {
            return Result.success()
        }

        return if (runAttemptCount < MAX_ATTEMPTS - 1) {
            Result.retry()
        } else {
            AppLogger.log("Push: token registration work exhausted retries")
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "push-token-registration"
        const val KEY_FCM_TOKEN = "fcm_token"
        private const val MAX_ATTEMPTS = 3
    }
}
