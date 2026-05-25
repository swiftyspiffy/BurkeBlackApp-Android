package com.swiftyspiffy.burkeblackapp.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.swiftyspiffy.burkeblackapp.data.api.ApiClient
import com.swiftyspiffy.burkeblackapp.util.AppLogger
import java.util.concurrent.TimeUnit

class StreamStatusWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val response = ApiClient.api.fetchStreamStatus()
            if (response.success && response.data != null) {
                val data = response.data
                WidgetDataStore.setStreamStatus(
                    appContext,
                    isLive = data.isLive,
                    title = data.title,
                    gameName = data.gameName,
                    viewerCount = data.viewerCount,
                    boxArtUrl = data.boxArtUrl,
                    startedAt = data.startedAt
                )
                StreamStatusSmallWidget().updateAll(appContext)
                StreamStatusMediumWidget().updateAll(appContext)
                AppLogger.log("Widget: stream status updated, live=${data.isLive}")
            }
            Result.success()
        } catch (e: Exception) {
            AppLogger.log("Widget: stream status fetch failed - ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "stream_status_widget"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StreamStatusWorker>(15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
