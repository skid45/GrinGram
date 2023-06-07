package com.skid.gringram.services

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.skid.gringram.R
import com.squareup.picasso.Picasso

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val sharedPreferences = getSharedPreferences("token", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("token", token)
            apply()
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val title = message.notification?.title
            val body = message.notification?.body
            val imageUrl = message.notification?.imageUrl

            val user = Person.Builder()
                .setName(title)
                .setIcon(IconCompat.createWithBitmap(Picasso.get().load(imageUrl).get()))
                .build()

            val notification =
                NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setStyle(
                        NotificationCompat.MessagingStyle(user)
                            .addMessage(body, System.currentTimeMillis(), user)
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1
    }
}