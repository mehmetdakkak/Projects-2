package com.joinup.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object BildirimHelper {
    private const val CHANNEL_ID = "joinup_events"
    private const val CHANNEL_NAME = "Etkinlik Hatırlatıcıları"

    fun kanalOlustur(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val kanal = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yaklaşan etkinlikler için hatırlatmalar"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(kanal)
        }
    }

    fun bildirimGoster(context: Context, baslik: String, mesaj: String, id: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bildirim = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(baslik)
            .setContentText(mesaj)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, bildirim)
    }

    fun hatirlaticiPlanla(context: Context, etkinlikId: String, etkinlikAdi: String, tarihSaat: String) {
        val tarihler = tarihSaatParse(tarihSaat) ?: return
        val simdi = System.currentTimeMillis()

        val birGunOnce = tarihler - (24 * 60 * 60 * 1000)
        if (birGunOnce > simdi) {
            isZamanla(context, etkinlikId, etkinlikAdi, birGunOnce - simdi, "1_gun", "yarın")
        }

        val birSaatOnce = tarihler - (60 * 60 * 1000)
        if (birSaatOnce > simdi) {
            isZamanla(context, etkinlikId, etkinlikAdi, birSaatOnce - simdi, "1_saat", "1 saat sonra")
        }

        val yarimSaatOnce = tarihler - (30 * 60 * 1000)
        if (yarimSaatOnce > simdi) {
            isZamanla(context, etkinlikId, etkinlikAdi, yarimSaatOnce - simdi, "30_dk", "30 dakika sonra")
        }
    }

    private fun isZamanla(context: Context, etkinlikId: String, etkinlikAdi: String, gecikmeMs: Long, tip: String, zamanMetni: String) {
        val data = workDataOf(
            "etkinlikId" to etkinlikId,
            "etkinlikAdi" to etkinlikAdi,
            "zamanMetni" to zamanMetni
        )

        val request = OneTimeWorkRequestBuilder<BildirimWorker>()
            .setInitialDelay(gecikmeMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("${etkinlikId}_$tip")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "${etkinlikId}_$tip",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun tarihSaatParse(tarihSaat: String): Long? {
        return try {
            SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale("tr")).parse(tarihSaat)?.time
        } catch (e: Exception) {
            try {
                SimpleDateFormat("dd/MM/yyyy", Locale("tr")).parse(tarihSaat)?.time
            } catch (e: Exception) { null }
        }
    }

    fun hatirlaticilariIptal(context: Context, etkinlikId: String) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag("${etkinlikId}_1_gun")
        workManager.cancelAllWorkByTag("${etkinlikId}_1_saat")
        workManager.cancelAllWorkByTag("${etkinlikId}_30_dk")
    }
}

class BildirimWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val etkinlikAdi = inputData.getString("etkinlikAdi") ?: "Etkinlik"
        val zamanMetni = inputData.getString("zamanMetni") ?: ""
        val id = inputData.getString("etkinlikId")?.hashCode() ?: System.currentTimeMillis().toInt()

        BildirimHelper.bildirimGoster(
            applicationContext,
            "⚽ $etkinlikAdi",
            "Etkinlik $zamanMetni başlayacak!",
            id
        )
        return Result.success()
    }
}
