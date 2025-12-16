package com.example.feastfast

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feastfast.adapter.MenuAdapter
import com.example.feastfast.databinding.FragmentMenuBottomSheetBinding
import com.example.feastfast.model.MenuItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MenuBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentMenuBottomSheetBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var menuItems: MutableList<MenuItem>
    private lateinit var menuAdapter: MenuAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBottomSheetBinding.inflate(inflater, container, false)

        binding.buttonBack.setOnClickListener {
            dismiss()
        }

        // 1. Initialize List
        menuItems = mutableListOf()

        // 2. Initialize Adapter ONCE
        menuAdapter = MenuAdapter(menuItems, requireContext())

        // 3. Setup RecyclerView
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = menuAdapter

        retrieveMenuItems()

        return binding.root
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()

        // Use "Menu" as per your instruction.
        // If this still fails, check the Logcat tag "DEBUG_DB" to see the real name.
        val foodRef: DatabaseReference = database.reference.child("Menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // DEBUGGING: Check if the node actually exists
                if (!snapshot.exists()) {
                    Log.e("DEBUG_DB", "Node 'Menu' does not exist! Check your database structure.")
                    // Let's print the root keys to see what is actually there
                    database.reference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(rootSnap: DataSnapshot) {
                            Log.d("DEBUG_DB", "Valid root nodes are: " + rootSnap.children.joinToString { it.key.toString() })
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })
                    return
                }

                menuItems.clear()

                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }

                Log.d("MenuBottomSheet", "Items Loaded: ${menuItems.size}")

                // FIX: notifyDataSetChanged should happen on the main thread, though Firebase usually handles this.
                // Added a safety check to ensure fragment is still valid.
                if (isAdded) {
                    menuAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MenuBottomSheet", "Database Error: ${error.message}")
            }
        })
    }
}
