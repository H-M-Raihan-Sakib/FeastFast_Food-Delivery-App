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
import com.example.feastfast.MainActivity
import com.example.feastfast.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificationService : Service() {

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start monitoring when service starts
        monitorOrderEvents()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun monitorOrderEvents() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Listen to the user's BuyHistory to get list of their Order IDs
        val historyRef = database.getReference("users").child(userId).child("BuyHistory")

        historyRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnapshot in snapshot.children) {
                    val orderId = orderSnapshot.key
                    if (orderId != null) {
                        // 2. Monitor the specific order in the main "OrderDetails" node
                        monitorSpecificOrder(orderId)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun monitorSpecificOrder(orderId: String) {
        val orderRef = database.getReference("OrderDetails").child(orderId)

        orderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                // Get boolean flags from database
                val orderAccepted = snapshot.child("orderAccepted").getValue(Boolean::class.java) ?: false
                val orderDispatch = snapshot.child("orderDispatch").getValue(Boolean::class.java) ?: false

                // SharedPreferences to prevent duplicate notifications
                val prefs = getSharedPreferences("NotifyPrefs", Context.MODE_PRIVATE)

                val acceptKey = "notify_${orderId}_accepted"
                val dispatchKey = "notify_${orderId}_dispatched"

                val alreadyNotifiedAccept = prefs.getBoolean(acceptKey, false)
                val alreadyNotifiedDispatch = prefs.getBoolean(dispatchKey, false)

                // ❗ CHECK 1: Order Accepted
                if (orderAccepted && !alreadyNotifiedAccept) {
                    sendNotification(
                        "Order Accepted",
                        "Your order has been accepted by the restaurant!",
                        orderId.hashCode() // Unique ID based on order hash
                    )
                    prefs.edit().putBoolean(acceptKey, true).apply()
                }

                // ❗ CHECK 2: Order Dispatched (Out for Delivery)
                if (orderDispatch && !alreadyNotifiedDispatch) {
                    sendNotification(
                        "Out for Delivery",
                        "Your order is out for delivery!",
                        orderId.hashCode() + 1 // Different ID so it doesn't overwrite the accept notification
                    )
                    prefs.edit().putBoolean(dispatchKey, true).apply()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendNotification(title: String, message: String, notificationId: Int) {
        val channelId = "FeastFastOrderChannel"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }
}
