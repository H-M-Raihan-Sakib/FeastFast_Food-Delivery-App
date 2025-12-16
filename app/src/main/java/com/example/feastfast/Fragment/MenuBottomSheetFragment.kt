package com.example.feastfast.Fragment

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

        // --- FIX STARTS HERE ---
        // 1. Initialize the list
        menuItems = mutableListOf()

        // 2. Initialize the adapter immediately with the empty list
        menuAdapter = MenuAdapter(menuItems, requireContext())

        // 3. Connect the adapter to the RecyclerView
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = menuAdapter
        // --- FIX ENDS HERE ---

        retrieveMenuItems()

        return binding.root
    }

    private fun retrieveMenuItems() {
        database = FirebaseDatabase.getInstance()
        // Ensure this matches your Firebase node exactly ("Menu" or "menu")
        val foodRef: DatabaseReference = database.reference.child("Menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Now it is safe to clear because menuItems was initialized in onCreateView
                menuItems.clear()

                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }

                Log.d("ITEMS", "ITEMS LOADED: " + menuItems.size)

                // Notify the adapter (which was initialized in onCreateView)
                menuAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("MenuBottomSheet", "Database Error: ${error.message}")
            }
        })
    }
}
