package com.example.lab_week_08

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class SecondNotificationService : Service() {

    private lateinit var builder: NotificationCompat.Builder
    private lateinit var handler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val channelId = createNotificationChannel()
        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Third worker process is done")
            .setContentText("Finalizing…")
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(getMainPendingIntent())

        startForeground(NOTIF_ID, builder.build())

        val ht = HandlerThread("SecondNotifThread").apply { start() }
        handler = Handler(ht.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post {
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            // countdown lebih singkat supaya aman dari tabrakan toast
            for (i in 5 downTo 0) {
                Thread.sleep(800L)
                builder.setContentText("$i seconds to finish")
                nm.notify(NOTIF_ID, builder.build())
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun getMainPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), flag
        )
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val id = "002"
            val name = "002 Channel"
            val mgr = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )
            mgr.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            )
            return id
        }
        return ""
    }

    companion object {
        const val NOTIF_ID = 0xCA8
        const val EXTRA_ID = "Id2"
    }
}