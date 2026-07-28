package com.linkside.app.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.linkside.app.MainActivity
import com.linkside.app.R

object PushNotificationHelper {
    const val CHANNEL_ID = "linkside_default"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Linkside",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Tee time invites, messages, and round reminders"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun showFromRemoteMessage(context: Context, message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val badge = message.data["badge"]?.toIntOrNull() ?: 0
        show(context, title, body, message.data, badge)
    }

    fun show(
        context: Context,
        title: String,
        body: String,
        data: Map<String, String>,
        badge: Int = 0,
    ) {
        if (!canPostNotifications(context)) return
        ensureChannel(context)

        val route = PushIntentParser.parseData(data)
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (route != null) PushIntentParser.applyToIntent(this, route)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            (route?.hashCode() ?: title.hashCode()),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setNumber(badge.coerceAtLeast(0))
            .build()

        NotificationManagerCompat.from(context).notify(
            (route?.hashCode() ?: title.hashCode()),
            notification,
        )
    }
}
