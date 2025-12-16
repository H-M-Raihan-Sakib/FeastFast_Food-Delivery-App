package com.example.feastfast.Fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.feastfast.ChatActivity
import com.example.feastfast.R
import com.example.feastfast.adapter.BuyAgainAdapter
import com.example.feastfast.databinding.FragmentHistoryBinding
import com.example.feastfast.model.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private val listOfOrderItem: MutableList<OrderDetails> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        retrieveOrderHistory()

        binding.chatWithUs.setOnClickListener {
            startActivity(Intent(requireContext(), ChatActivity::class.java))
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        buyAgainAdapter = BuyAgainAdapter(
            arrayListOf(),
            arrayListOf(),
            arrayListOf(),
            requireContext()
        )
        binding.BuyAgainRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.BuyAgainRecyclerView.adapter = buyAgainAdapter
    }

    private fun retrieveOrderHistory() {
        binding.cardViewRecent.visibility = View.GONE
        binding.textViewPrevious.visibility = View.GONE
        binding.BuyAgainRecyclerView.visibility = View.GONE

        userId = auth.currentUser?.uid ?: ""

        if (userId.isEmpty()) {
            Log.e("HistoryDebug", "User not logged in")
            return
        }

        val buyItemRef =
            database.reference.child("users").child(userId).child("BuyHistory")

        buyItemRef.orderByChild("orderTime")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    listOfOrderItem.clear()

                    for (buySnapshot in snapshot.children) {
                        val orderItem =
                            buySnapshot.getValue(OrderDetails::class.java)
                        if (orderItem != null) {
                            listOfOrderItem.add(orderItem)
                        }
                    }

                    listOfOrderItem.reverse()

                    if (listOfOrderItem.isNotEmpty()) {
                        setDataInRecentBuy()
                        setPreviousBuyItems()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("HistoryDebug", error.message)
                }
            })
    }

    private fun setDataInRecentBuy() {
        binding.cardViewRecent.visibility = View.VISIBLE
        binding.cardViewOrderStatus.visibility = View.VISIBLE

        val recentItem = listOfOrderItem.firstOrNull() ?: return

        // 1. Set Static Info (Name, Price, Image) from History List immediately
        binding.buyAgainFoodName.text =
            recentItem.foodName?.firstOrNull() ?: "Unknown"
        binding.buyAgainFoodPrice.text =
            recentItem.foodPrice?.firstOrNull() ?: "$0"

        val image = recentItem.foodImage?.firstOrNull()
        if (!image.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(Uri.parse(image))
                .into(binding.buyAgainFoodImage)
        }

        // 2. Fetch LIVE Status from "OrderDetails" Node
        // This ensures we see the update even if the Admin didn't update the user's BuyHistory
        val pushKey = recentItem.itemPushKey
        if (pushKey != null) {
            val liveOrderRef = database.reference.child("OrderDetails").child(pushKey)

            // Use addValueEventListener to see changes in real-time
            liveOrderRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val liveOrder = snapshot.getValue(OrderDetails::class.java)
                        liveOrder?.let {
                            updateStatusColors(it)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // If fetching live data fails, fallback to the history item
                    updateStatusColors(recentItem)
                }
            })
        } else {
            updateStatusColors(recentItem)
        }
    }

    private fun updateStatusColors(item: OrderDetails) {
        // Order Status Text
        binding.orderStatus.text = when {
            item.paymentReceived -> "Paid"
            item.orderDispatch -> "Sent"
            item.orderAccepted -> "Accepted"
            else -> "Pending"
        }

        val colorActive = android.graphics.Color.parseColor("#4CAF50") // Green
        val colorInactive = android.graphics.Color.parseColor("#E0E0E0") // Grey

        // --- Step 1: Accepted ---
        binding.statusImageAccepted.setImageResource(
            if (item.orderAccepted)
                R.drawable.shape_status_active
            else
                R.drawable.shape_status_inactive
        )

        // --- Step 2: Sent (Dispatched) ---
        binding.statusImageSent.setImageResource(
            if (item.orderDispatch)
                R.drawable.shape_status_active
            else
                R.drawable.shape_status_inactive
        )

        // Line between Accepted -> Sent turns green if Sent is active
        binding.statusLine1.setBackgroundColor(
            if (item.orderDispatch) colorActive else colorInactive
        )

        // --- Step 3: Payment Received ---
        binding.statusImagePayment.setImageResource(
            if (item.paymentReceived)
                R.drawable.shape_status_active
            else
                R.drawable.shape_status_inactive
        )

        // Line between Sent -> Payment turns green if Payment is active
        binding.statusLine2.setBackgroundColor(
            if (item.paymentReceived) colorActive else colorInactive
        )
    }

    private fun setPreviousBuyItems() {
        binding.textViewPrevious.visibility = View.VISIBLE
        binding.BuyAgainRecyclerView.visibility = View.VISIBLE

        val names = arrayListOf<String>()
        val prices = arrayListOf<String>()
        val images = arrayListOf<String>()

        for (order in listOfOrderItem) {
            order.foodName?.let { names.addAll(it) }
            order.foodPrice?.let { prices.addAll(it) }
            order.foodImage?.let { images.addAll(it) }
        }

        buyAgainAdapter =
            BuyAgainAdapter(names, prices, images, requireContext())

        binding.BuyAgainRecyclerView.adapter = buyAgainAdapter
    }
}
