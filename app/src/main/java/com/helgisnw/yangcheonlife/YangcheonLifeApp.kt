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
        val currentTopic = "$currentGrade-$currentClass"

        android.util.Log.d("FCM_DEBUG", "앱 시작 - FCM 토픽 구독 확인 및 업데이트")
        android.util.Log.d("FCM_DEBUG", "현재 설정된 학년/반: $currentGrade-$currentClass")

        // 현재 토큰 가져오기 (로깅용)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                android.util.Log.d("FCM_DEBUG", "현재 FCM 토큰: ${task.result}")
            } else {
                android.util.Log.e("FCM_DEBUG", "FCM 토큰 가져오기 실패", task.exception)
            }
        }

        // 모든 가능한 토픽에서 구독 취소 (현재 토픽 제외)
        for (grade in 1..3) {
            for (classNum in 1..11) {
                val topic = "$grade-$classNum"
                if (grade != currentGrade || classNum != currentClass) {
                    android.util.Log.d("FCM_DEBUG", "불필요한 FCM 토픽 구독 해제: $topic")
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                android.util.Log.d("FCM_DEBUG", "FCM 토픽 구독 해제 성공: $topic")
                            } else {
                                android.util.Log.e("FCM_DEBUG", "FCM 토픽 구독 해제 실패: $topic", task.exception)
                            }
                        }
                }
            }
        }

        // 현재 토픽 구독
        android.util.Log.d("FCM_DEBUG", "현재 FCM 토픽 구독: $currentTopic")
        FirebaseMessaging.getInstance().subscribeToTopic(currentTopic)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    android.util.Log.d("FCM_DEBUG", "FCM 토픽 구독 성공: $currentTopic")
                } else {
                    android.util.Log.e("FCM_DEBUG", "FCM 토픽 구독 실패: $currentTopic", task.exception)
                }
            }
    }
}