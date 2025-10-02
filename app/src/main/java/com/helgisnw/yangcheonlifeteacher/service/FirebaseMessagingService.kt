package com.helgisnw.yangcheonlifeteacher.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.helgisnw.yangcheonlifeteacher.MainActivity
import com.helgisnw.yangcheonlifeteacher.R
import com.helgisnw.yangcheonlifeteacher.YangcheonLifeApp.Companion.CHANNEL_ID

class YangcheonMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        android.util.Log.d("FCM_DEBUG", "FCM 메시지 수신: ${remoteMessage.data}")

        remoteMessage.notification?.let { notification ->
            android.util.Log.d("FCM_DEBUG", "알림 제목: ${notification.title}, 내용: ${notification.body}")
            showNotification(notification.title, notification.body)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        android.util.Log.d("FCM_DEBUG", "새 FCM 토큰: $token")
        // TODO: 필요한 경우 토큰을 서버에 전송
    }

    private fun showNotification(title: String?, messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}