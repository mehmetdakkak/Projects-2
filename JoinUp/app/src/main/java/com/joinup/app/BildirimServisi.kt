package com.joinup.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BildirimServisi : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let { notification ->
            bildirimGoster(
                notification.title ?: "JoinUp",
                notification.body ?: ""
            )
        }

        remoteMessage.data.isNotEmpty().let {
            val baslik = remoteMessage.data["title"] ?: "JoinUp"
            val mesaj = remoteMessage.data["body"] ?: ""
            if (mesaj.isNotEmpty()) {
                bildirimGoster(baslik, mesaj)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    private fun bildirimGoster(baslik: String, mesaj: String) {
        val channelId = "joinup_notifications"

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "JoinUp Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Etkinlik ve mesaj bildirimleri"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
