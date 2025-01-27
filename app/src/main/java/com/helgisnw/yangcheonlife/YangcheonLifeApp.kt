package com.helgisnw.yangcheonlife

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class YangcheonLifeApp : Application() {

    companion object {
        const val CHANNEL_ID = "yangcheon_notifications"
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
        checkAndUpdateTopicSubscription()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkAndUpdateTopicSubscription() {
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentGrade = prefs.getInt("defaultGrade", 1)
        val currentClass = prefs.getInt("defaultClass", 1)

        // Unsubscribe from all possible topics except current
        for (grade in 1..3) {
            for (classNum in 1..11) {
                if (grade != currentGrade || classNum != currentClass) {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("$grade-$classNum")
                }
            }
        }

        // Subscribe to current topic
        FirebaseMessaging.getInstance().subscribeToTopic("$currentGrade-$currentClass")
    }
}