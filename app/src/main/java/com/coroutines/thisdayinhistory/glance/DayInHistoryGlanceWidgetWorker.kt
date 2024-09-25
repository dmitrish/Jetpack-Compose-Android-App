package com.coroutines.thisdayinhistory.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.coroutines.data.models.HistoricalEvent
import java.util.concurrent.TimeUnit


sealed interface WidgetState {
    data object Empty : WidgetState
    data object Loading : WidgetState
    data class Loaded (val data: List<HistoricalEvent>) : WidgetState
}

class GlanceWidgetWorker(
    private val appContext: Context,
    private val workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val glanceId = GlanceAppWidgetManager(appContext)
            .getGlanceIds(DayInHistoryGlanceAppWidget::class.java).firstOrNull()

        glanceId?.let {
            DayInHistoryGlanceAppWidget().apply {
                update(appContext, glanceId)
            }
        }

        return Result.success()
    }

    /*private suspend fun updateWidget(imageUri: String) {
        ThisDayInHistoryGlanceAppWidget().apply {
            updateAppWidgetState(appContext, glanceId) {
                update(it)
            }
            update(appContext, glanceId)
        }
    }*/
}

fun Context.starGlanceWorker() {
    val networkConstraint =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    val request = PeriodicWorkRequest
        .Builder(GlanceWidgetWorker::class.java, 15, TimeUnit.MINUTES)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5000L, TimeUnit.MILLISECONDS)
        .setConstraints(networkConstraint)
        .build()
    val uniqueTag = DayInHistoryGlanceAppWidget.UNIQUE_WORK_TAG
    WorkManager.getInstance(this)
        .enqueueUniquePeriodicWork(
            uniqueTag,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
}
