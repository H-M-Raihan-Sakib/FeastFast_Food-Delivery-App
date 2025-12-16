package com.example.feastfast

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.feastfast.databinding.ActivityMainBinding
import com.example.feastfast.service.ChatNotificationService
import com.example.feastfast.service.NotificationService
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isNotificationEnabled = true // Default state is ON

    // Notification Permission Launcher (for Android 13 and above)
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // If permission is granted AND the user wants notifications, start the service
            if (isNotificationEnabled) {
                startOrderListenerService()
            }
        } else {
            // If permission is denied, force the toggle to OFF
            isNotificationEnabled = false
            updateNotificationPreference()
            Toast.makeText(this, "Notifications permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- 1. MOVED THEME LOGIC HERE (Merged from the duplicate function) ---
        val sharedPrefs = getSharedPreferences("FeastFastPrefs", MODE_PRIVATE)
        val isDarkModeOn = sharedPrefs.getBoolean("dark_mode", false)

        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // ---------------------------------------------------------------------

        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Load the user's saved notification preference (e.g., from a previous session)
        loadNotificationPreference()

        // Setup bottom navigation
        val navController: NavController = findNavController(R.id.fragmentContainerView5)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView2)
        bottomNav.setupWithNavController(navController)

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.notificationButton.setOnClickListener {
            // Invert the current state
            isNotificationEnabled = !isNotificationEnabled

            // Save the new state and update the UI/Service
            updateNotificationPreference()
        }

        updateNotificationState()
    }

    private fun loadNotificationPreference() {
        val prefs = getSharedPreferences("FeastFastPrefs", MODE_PRIVATE)
        // Load the saved preference, defaulting to 'true' (on) if it doesn't exist
        isNotificationEnabled = prefs.getBoolean("notification_enabled", true)
    }

    private fun updateNotificationPreference() {
        val prefs = getSharedPreferences("FeastFastPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("notification_enabled", isNotificationEnabled).apply()
        updateNotificationState()
    }

    private fun updateNotificationState() {
        if (isNotificationEnabled) {
            // State is ON
            binding.notificationButton.setImageResource(R.drawable.ic_notifications_active)
            // Check for permission before attempting to start the service
            checkNotificationPermission()
        } else {
            // State is OFF
            binding.notificationButton.setImageResource(R.drawable.ic_notifications_off)
            // Stop the background service
            stopOrderListenerService()
        }
    }

    private fun checkNotificationPermission() {
        // Runtime permission is only required for Android 13 (TIRAMISU) and higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, so start the service
                startOrderListenerService()
            } else {
                // Permission is not granted, so request it
                requestNotificationPermission.launch(permission)
            }
        } else {
            // For Android 12 and lower, no runtime permission is needed, just start the service
            startOrderListenerService()
        }
    }

    private fun startOrderListenerService() {
        startService(Intent(this, NotificationService::class.java))
        startService(Intent(this, ChatNotificationService::class.java))
    }

    private fun stopOrderListenerService() {
        val intent = Intent(this, NotificationService::class.java)
        stopService(intent)
    }
}
