package com.helgisnw.yangcheonlifeteacher.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object TeacherScheduleWidgetUpdater {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun requestImmediateUpdate(context: Context) {
        scope.launch {
            TeacherScheduleWidget().updateAll(context)
        }
    }

    fun ensurePeriodicRefresh(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val request = PeriodicWorkRequestBuilder<TeacherScheduleWidgetRefreshWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            TeacherScheduleWidgetRefreshWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
