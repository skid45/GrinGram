package com.skid.gringram.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DecoratedCustomViewStyle
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skid.gringram.R
import com.skid.gringram.ui.MainActivity
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.CircleImageTransformation
import com.skid.gringram.utils.Constants.COMPANION_USER
import com.skid.gringram.utils.Constants.NOTIFICATION
import com.skid.gringram.utils.Constants.SHARED_PREF_CHAT_NOTIFICATIONS_SOUND
import com.skid.gringram.utils.Constants.SHARED_PREF_TOKEN
import com.squareup.picasso.Picasso

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sharedPreferences = getSharedPreferences(SHARED_PREF_TOKEN, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(SHARED_PREF_TOKEN, token)
            apply()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val userUid = message.data["userUid"]!!
        val chatNotificationsSharedPref =
            getSharedPreferences(SHARED_PREF_CHAT_NOTIFICATIONS_SOUND, Context.MODE_PRIVATE)
        val chatNotificationForUser =
            chatNotificationsSharedPref.getString(userUid, Dialog.SOUND_ON)

        if (chatNotificationForUser == Dialog.SOUND_ON) {
            if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                val mediaPlayer = MediaPlayer.create(this, R.raw.notification_sound)
                mediaPlayer.start()

            } else {
                val title = message.data["title"]!!
                val body = message.data["body"]!!
                val imageUrl = message.data["userImageUrl"]

                val bundle = bundleOf(
                    COMPANION_USER to User(
                        uid = userUid,
                        username = title,
                        photoUri = imageUrl.toString()
                    ),
                    NOTIFICATION to true
                )

                val intent = Intent(this, MainActivity::class.java)
                intent.putExtras(bundle)
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                val notificationLayout =
                    RemoteViews(packageName, R.layout.custom_notification_layout)
                notificationLayout.apply {
                    setImageViewBitmap(
                        R.id.notification_user_image,
                        Picasso.get().load(imageUrl).transform(CircleImageTransformation()).get()
                    )
                    setTextViewText(R.id.notification_title, title)
                    setTextViewText(R.id.notification_body, body)
                    setTextColor(R.id.notification_title, getColor(R.color.colorOnPrimary))
                    setTextColor(R.id.notification_body, getColor(R.color.colorOnPrimary))
                }

                val notification =
                    NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                        .setSmallIcon(R.drawable.icon_app_24)
                        .setStyle(DecoratedCustomViewStyle())
                        .setCustomContentView(notificationLayout)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(MainActivity.NOTIFICATION_ID, notification.build())
            }
        }
    }
}