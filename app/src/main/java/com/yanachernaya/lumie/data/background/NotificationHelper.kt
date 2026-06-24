package com.yanachernaya.lumie.data.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.presentation.MainActivity
import com.yanachernaya.lumie.presentation.ui.theme.Lavender400
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.new_affirmation_name_of_channel),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.new_affirmation_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showNewAffirmationNotification(affirmation: String) {
        createNotificationChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            PENDING_INTENT_RC,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_bell)
            .setColor(Lavender400.toArgb())
            .setContentTitle(context.getString(R.string.new_affirmation_notification_title))
            .setContentText(affirmation)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            NOTIFICATION_ID,
            notification
        )
    }

    companion object {
        private const val CHANNEL_ID = "new_affirmation"
        private const val PENDING_INTENT_RC = 1
        private const val NOTIFICATION_ID = 1
    }
}