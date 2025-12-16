package com.example.feastfast.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feastfast.adapter.MenuAdapter
import com.example.feastfast.databinding.FragmentSearchBinding
import com.example.feastfast.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var database: FirebaseDatabase

    // We keep the original full list to filter against
    private val originalMenuItems = mutableListOf<MenuItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // 1. Setup RecyclerView immediately with empty list to avoid "No adapter attached" errors
        adapter = MenuAdapter(mutableListOf(), requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance()

        retrieveMenuItems()
        setupSearchView()

        return binding.root
    }


    private fun retrieveMenuItems() {
        val foodReference: DatabaseReference = database.reference.child("Menu") // Ensure "Menu" matches DB

        foodReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                originalMenuItems.clear()

                // Debug Log
                android.util.Log.d("SearchFragment", "Items found: ${snapshot.childrenCount}")

                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        originalMenuItems.add(it)
                    }
                }
                showAllMenu()
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("SearchFragment", "Error: ${error.message}")
            }
        })
    }


    private fun showAllMenu() {
        setAdapter(originalMenuItems)
    }

    private fun setAdapter(filteredList: List<MenuItem>) {
        adapter = MenuAdapter(filteredList, requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String?) {
        val filteredList = mutableListOf<MenuItem>()

        if (!query.isNullOrEmpty()) {
            for (item in originalMenuItems) {
                // Filter by Food Name (case insensitive)
                if (item.foodName?.contains(query, ignoreCase = true) == true) {
                    filteredList.add(item)
                }
            }
            setAdapter(filteredList)
        } else {
            // If search is empty, show the full original list
            showAllMenu()
        }
    }
}
