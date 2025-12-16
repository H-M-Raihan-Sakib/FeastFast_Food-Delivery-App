package com.example.feastfast.adapterimport

import android.content.Context

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.feastfast.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,      // Food Names
    private val cartItemPrices: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartDescriptions: MutableList<String>,
    private val cartQuantities: MutableList<Int>,
    private val cartIngredients: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Initialize Auth
    private val auth = FirebaseAuth.getInstance()
    private lateinit var cartItemsReference: DatabaseReference

    // Changed from 'val' to 'var' so it can be reassigned when items are deleted.
    private var itemQuantities: IntArray

    init {
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        // Initialize the database reference for this specific user instance
        cartItemsReference = database.reference.child("users").child(userId).child("CartItems")

        // Initialize quantities array based on the initial size of the cart items list
        itemQuantities = IntArray(cartItems.size) { 1 }
    }

    // REMOVED the buggy companion object

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    // Helper function to return updated quantities to the Fragment
    fun getUpdateItemsQuantities(): MutableList<Int> {
        val quantityList = mutableListOf<Int>()
        itemQuantities.forEach { quantityList.add(it) }
        return quantityList
    }

    override fun getItemCount(): Int = cartItems.size

    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                // Sync local array with Firebase data initially
                if (position < cartQuantities.size && position < itemQuantities.size) {
                    itemQuantities[position] = cartQuantities[position]
                }

                val quantity = if (position < itemQuantities.size) itemQuantities[position] else 1

                // Set Data to Views
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = cartItemPrices[position]

                // Load Image using Glide
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)

                cartItemquantity.text = quantity.toString()

                minusButton.setOnClickListener {
                    decreaseQuantity(adapterPosition)
                }
                plusButton.setOnClickListener {
                    increaseQuantity(adapterPosition)
                }
                deleteButton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        private fun increaseQuantity(position: Int) {
            if (position != RecyclerView.NO_POSITION && itemQuantities[position] < 10) {
                itemQuantities[position]++
                binding.cartItemquantity.text = itemQuantities[position].toString()
                updateFirebaseQuantity(position, itemQuantities[position])
            }
        }

        private fun decreaseQuantity(position: Int) {
            if (position != RecyclerView.NO_POSITION && itemQuantities[position] > 1) {
                itemQuantities[position]--
                binding.cartItemquantity.text = itemQuantities[position].toString()
                updateFirebaseQuantity(position, itemQuantities[position])
            }
        }

        private fun updateFirebaseQuantity(position: Int, newQuantity: Int) {
            if(position < cartItems.size) {
                val uniqueKey = cartItems[position]
                val quantityMap = mapOf<String, Any>("foodQuanitity" to newQuantity)
                cartItemsReference.child(uniqueKey).updateChildren(quantityMap)
            }
        }

        private fun deleteItem(position: Int) {
            val uniqueKey = cartItems[position]

            cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                if (position < cartItems.size) {
                    cartItems.removeAt(position)
                    cartImages.removeAt(position)
                    cartDescriptions.removeAt(position)
                    cartQuantities.removeAt(position)
                    cartItemPrices.removeAt(position)
                    cartIngredients.removeAt(position)

                    // Update the quantities array by removing the item at the specific index
                    itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toIntArray()

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItems.size)

                    Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
