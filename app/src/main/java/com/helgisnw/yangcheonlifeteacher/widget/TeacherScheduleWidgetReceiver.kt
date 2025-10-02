package com.helgisnw.yangcheonlifeteacher.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class TeacherScheduleWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TeacherScheduleWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        TeacherScheduleWidgetUpdater.ensurePeriodicRefresh(context)
        TeacherScheduleWidgetUpdater.requestImmediateUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            TeacherScheduleWidgetUpdater.requestImmediateUpdate(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.helgisnw.yangcheonlifeteacher.widget.ACTION_REFRESH"
    }
}
