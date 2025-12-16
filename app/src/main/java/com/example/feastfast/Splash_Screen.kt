package com.example.feastfast

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.feastfast.service.ChatNotificationService
import com.google.firebase.auth.FirebaseAuth

class Splash_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({

            // 1. Check if user is already logged in
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                // 2. User is logged in -> Go DIRECTLY to MainActivity
                startService(Intent(this, ChatNotificationService::class.java))
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // 3. User is NOT logged in -> Go to StartActivity (or LoginActivity)
                val intent = Intent(this, StartActivity::class.java)
                startActivity(intent)
                finish()
            }

        }, 3000) // 3000ms delay
    }
}
