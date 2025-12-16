package com.example.feastfast

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.feastfast.databinding.FragmentCongratesBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CongratesBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCongratesBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCongratesBottomSheetBinding.inflate(layoutInflater, container, false)
        binding.goHome.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    companion object {

    }
}