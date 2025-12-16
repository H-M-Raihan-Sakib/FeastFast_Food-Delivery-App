package com.example.feastfast.Fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.feastfast.PayOutActivity
import com.example.feastfast.adapterimport.CartAdapter
import com.example.feastfast.databinding.FragmentCartBinding
import com.example.feastfast.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var foodNames: MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImagesUri: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var foodQuantities: MutableList<Int>
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialize Database reference early to avoid null pointer issues
        database = FirebaseDatabase.getInstance().reference
        userId = auth.currentUser?.uid ?: ""

        retrieveCartItems()

        binding.proceedButton.setOnClickListener {
            getOrderItemDetail()
        }

        return binding.root
    }

    private fun getOrderItemDetail() {
        val orderIdReference: DatabaseReference = database.child("users").child(userId).child("CartItems")

        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodImage = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()

        // Use the updated quantities from the adapter (user might have changed them in the UI)
        // Assuming CartAdapter has a method getUpdatedQuantities() returning MutableList<Int>
        val foodQuantity = cartAdapter.getUpdateItemsQuantities()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val orderItem = foodSnapshot.getValue(CartItem::class.java)

                    orderItem?.foodName?.let { foodName.add(it) }
                    orderItem?.foodPrice?.let { foodPrice.add(it) }
                    orderItem?.foodDescription?.let { foodDescription.add(it) }
                    orderItem?.foodImage?.let { foodImage.add(it) }
                    orderItem?.foodIngredient?.let { foodIngredient.add(it) }
                }

                orderNow(foodName, foodPrice, foodDescription, foodImage, foodQuantity, foodIngredient)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Data not fetched", Toast.LENGTH_SHORT).show()
                Log.d("CartFragment", "Database Error: ${error.message}")
            }
        })
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodImage: MutableList<String>,
        foodQuantity: MutableList<Int>,
        foodIngredient: MutableList<String>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent.putExtra("FoodName", foodName as ArrayList<String>)
            intent.putExtra("FoodPrice", foodPrice as ArrayList<String>)
            intent.putExtra("FoodDescription", foodDescription as ArrayList<String>)
            intent.putExtra("FoodImage", foodImage as ArrayList<String>)
            intent.putExtra("FoodIngredient", foodIngredient as ArrayList<String>)
            intent.putExtra("FoodQuantity", foodQuantity as ArrayList<Int>)

            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
        // Ensure userId is initialized
        userId = auth.currentUser?.uid ?: ""
        val foodReference: DatabaseReference =
            database.child("users").child(userId).child("CartItems")

        // Initialize lists
        foodNames = mutableListOf()
        foodPrices = mutableListOf()
        foodDescriptions = mutableListOf()
        foodImagesUri = mutableListOf()
        foodIngredients = mutableListOf()
        foodQuantities = mutableListOf()

        foodReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear lists before adding new data to prevent duplication
                foodNames.clear()
                foodPrices.clear()
                foodDescriptions.clear()
                foodImagesUri.clear()
                foodQuantities.clear()
                foodIngredients.clear()

                for (foodSnapshot in snapshot.children) {
                    val cartItems = foodSnapshot.getValue(CartItem::class.java)
                    cartItems?.foodName?.let { foodNames.add(it) }
                    cartItems?.foodPrice?.let { foodPrices.add(it) }
                    cartItems?.foodDescription?.let { foodDescriptions.add(it) }
                    cartItems?.foodImage?.let { foodImagesUri.add(it) }
                    cartItems?.foodQuanitity?.let { foodQuantities.add(it) }
                    cartItems?.foodIngredient?.let { foodIngredients.add(it) }
                }

                // Call the helper function to update UI
                setAdapter()
            }

            override fun onCancelled(error: DatabaseError) {
                if(isAdded) {
                    Toast.makeText(requireContext(), "Data not fetched", Toast.LENGTH_SHORT).show()
                }
                Log.d("CartFragment", "Database Error: ${error.message}")
            }
        })
    }

    private fun setAdapter() {
        if (context != null) {
            cartAdapter = CartAdapter(
                requireContext(),
                foodNames,
                foodPrices,
                foodImagesUri,
                foodDescriptions,
                foodQuantities,
                foodIngredients
            )

            binding.cartRecyclerView.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.cartRecyclerView.adapter = cartAdapter
        }
    }
}
