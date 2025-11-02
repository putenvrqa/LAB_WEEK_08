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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // 1) Siapkan channel + builder, lalu startForeground
        val channelId = createNotificationChannel()
        notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second worker process is done")
            .setContentText("Check it out!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(getMainPendingIntent())

        startForeground(NOTIF_ID, notificationBuilder.build())

        // 2) Thread khusus untuk kerja background
        val ht = HandlerThread("NotificationServiceThread").apply { start() }
        serviceHandler = Handler(ht.looper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getStringExtra(EXTRA_ID) ?: "001"

        serviceHandler.post {
            // Count down 10..0 dan update notif tiap detik
            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            for (i in 10 downTo 0) {
                Thread.sleep(1000L)
                notificationBuilder.setContentText("$i seconds until last warning")
                nm.notify(NOTIF_ID, notificationBuilder.build())
            }

            // Beri tahu MainActivity kalau selesai
            mutableID.postValue(id)

            // Bereskan service foreground
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
            val id = "001"
            val name = "001 Channel"
            val manager = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            )
            return id
        }
        return "" // pre-O
    }

    companion object {
        const val NOTIF_ID = 0xCA7
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}