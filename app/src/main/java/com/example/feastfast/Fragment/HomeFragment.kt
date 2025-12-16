package com.example.feastfast.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.feastfast.R
import com.example.feastfast.adapter.PopularAdapter
import com.example.feastfast.databinding.FragmentHomeBinding
import com.example.feastfast.model.MenuItem
import com.example.feastfast.model.Rating
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.viewAllMenu.setOnClickListener {
            val bottomSheetDialog = MenuBottomSheetFragment()
            bottomSheetDialog.show(parentFragmentManager, "Test")
        }
        retreiveAndPopularItems()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageList = ArrayList<SlideModel>()
        imageList.add(SlideModel(R.drawable.banner, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner1, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, ScaleTypes.FIT))

        val imageSlider = binding.imageSlider
        imageSlider.setImageList(imageList, ScaleTypes.FIT)
        imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                // You can handle clicks on the slider images here
            }
            override fun doubleClick(position: Int) {}
        })
    }

    private fun retreiveAndPopularItems() {
        database = FirebaseDatabase.getInstance()
        val foodRef = database.reference.child("Menu")

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Safety check: if fragment is detached, stop
                if (!isAdded) return

                val items = mutableListOf<MenuItem>()
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let { items.add(it) }
                }
                fetchRatingsAndDisplay(items)
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchRatingsAndDisplay(menuItems: MutableList<MenuItem>) {
        val ratingsRef = database.reference.child("ratings")
        ratingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Safety check
                if (!isAdded) return

                for (menuItem in menuItems) {
                    val foodName = menuItem.foodName ?: continue
                    val ratingNode = snapshot.child(foodName)

                    if (ratingNode.exists()) {
                        var totalRating = 0f
                        var ratingCount = 0
                        for (ratingSnapshot in ratingNode.children) {
                            val rating = ratingSnapshot.getValue(Rating::class.java)
                            rating?.let {
                                totalRating += it.ratingValue
                                ratingCount++
                            }
                        }
                        if (ratingCount > 0) {
                            menuItem.averageRating = totalRating / ratingCount
                        }
                    }
                }
                sortAndDisplayPopularItems(menuItems)
            }
            override fun onCancelled(error: DatabaseError) {
                // Even if ratings fail, try to display items
                if (isAdded) {
                    sortAndDisplayPopularItems(menuItems)
                }
            }
        })
    }

    private fun sortAndDisplayPopularItems(menuItems: List<MenuItem>) {
        val sortedMenuItems = menuItems.sortedByDescending { it.averageRating }
        val popularItems = sortedMenuItems.take(6)
        setPopularItems(popularItems)
    }

    private fun setPopularItems(popularItems: List<MenuItem>) {
        // --- FIX: CRITICAL CRASH PREVENTION ---
        // Check if the fragment is currently added to its activity.
        // If not, 'requireContext()' would crash.
        if (!isAdded) return

        // Use 'context' safely or 'requireContext()' now that we checked isAdded
        val adapter = PopularAdapter(popularItems, requireContext())
        binding.PopulerRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.PopulerRecyclerView.adapter = adapter
    }
}
