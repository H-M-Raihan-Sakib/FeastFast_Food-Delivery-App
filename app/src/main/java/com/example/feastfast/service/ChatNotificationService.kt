package com.example.feastfast.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.feastfast.ChatActivity
import com.example.feastfast.R
import com.example.feastfast.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatNotificationService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var chatListener: ChildEventListener? = null
    private var chatRef: DatabaseReference? = null

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Remove any old listener before starting a new one to prevent duplicates
            if (chatListener != null && chatRef != null) {
                chatRef?.removeEventListener(chatListener!!)
            }
            // Start listening for messages for the current logged-in user
            listenForAdminReplies(userId)
        }
        // This ensures the service tries to restart if the system kills it
        return START_STICKY
    }

    private fun listenForAdminReplies(userId: String) {
        chatRef = database.reference.child("chats").child(userId)

        chatListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(ChatMessage::class.java)

                // Check if the message is from an admin
                // It uses "sentByAdmin" to match your ChatMessage model.
                if (message != null && message.sentByAdmin) {
                    // Only show a notification if the message is recent (e.g., within the last minute)
                    // This prevents old admin messages from triggering notifications every time the app starts.
                    val oneMinuteAgo = System.currentTimeMillis() - 60000
                    if (message.timestamp > oneMinuteAgo) {
                        showNotification(message.message ?: "You have a new message from Support.")
                    }
                }
            }

            // These overrides must be here but are not needed for our specific logic
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        // Attach the listener to the database reference
        chatRef?.addChildEventListener(chatListener!!)
    }

    private fun showNotification(message: String) {
        val channelId = "ChatNotificationChannel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8.0 (Oreo) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Support Chat Replies",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        // Create an intent that will open ChatActivity when the notification is tapped
        val intent = Intent(this, ChatActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Build the notification itself
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Make sure this icon exists in your project
            .setContentTitle("New Message from Support")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // Set the action to perform on tap
            .setAutoCancel(true) // The notification will disappear after being tapped
            .build()

        // Show the notification. Use a unique ID (based on current time) to avoid overwriting previous notifications.
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up the listener when the service is destroyed to prevent memory leaks
        if (chatListener != null && chatRef != null) {
            chatRef?.removeEventListener(chatListener!!)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Return null because we are not using a bound service
        return null
    }
}
    