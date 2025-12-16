package com.example.feastfast.Fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.feastfast.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // 1. Initialize the Switch State
        setupDarkModeSwitch()

        // ... existing logic for user data ...

        return binding.root
    }

    private fun setupDarkModeSwitch() {
        val sharedPrefs = requireContext().getSharedPreferences("FeastFastPrefs", Context.MODE_PRIVATE)

        // Load saved state (default is false/Light mode)
        val isDarkModeOn = sharedPrefs.getBoolean("dark_mode", false)
        binding.darkModeSwitch.isChecked = isDarkModeOn

        // Listener for changes
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the new state
            sharedPrefs.edit().putBoolean("dark_mode", isChecked).apply()

            // Apply the theme immediately
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}
