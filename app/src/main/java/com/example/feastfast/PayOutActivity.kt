package com.example.feastfast

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.feastfast.databinding.ActivityPayOutBinding
import com.example.feastfast.model.OrderDetails // Ensure you have this model class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PayOutActivity : AppCompatActivity() {
    lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var name: String
    private lateinit var phone: String
    private lateinit var address: String
    private lateinit var totalAmount: String
    private lateinit var foodName: ArrayList<String>
    private lateinit var foodPrice: ArrayList<String>
    private lateinit var foodDescription: ArrayList<String>
    private lateinit var foodImage: ArrayList<String>
    private lateinit var foodIngredient: ArrayList<String>
    private lateinit var foodQuantity: ArrayList<Int>
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference

        setUserData()

        // Get details from Intent (Safely)
        val intent = intent
        foodName = intent.getStringArrayListExtra("FoodName") ?: arrayListOf()
        foodPrice = intent.getStringArrayListExtra("FoodPrice") ?: arrayListOf()
        foodDescription = intent.getStringArrayListExtra("FoodDescription") ?: arrayListOf()
        foodImage = intent.getStringArrayListExtra("FoodImage") ?: arrayListOf()
        foodIngredient = intent.getStringArrayListExtra("FoodIngredient") ?: arrayListOf()
        foodQuantity = intent.getIntegerArrayListExtra("FoodQuantity") ?: arrayListOf()

        // FIX 1: calculateTotalAmount takes no arguments, so don't pass them
        totalAmount = calculateTotalAmount().toString() + "$"
        binding.totalAmount.isEnabled = false
        binding.totalAmount.setText(totalAmount)

        binding.PlaceMyOrder.setOnClickListener {
            // Get current values from Edit Texts
            name = binding.name.text.toString().trim()
            phone = binding.phone.text.toString().trim()
            address = binding.address.text.toString().trim()

            if (name.isBlank() || phone.isBlank() || address.isBlank()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show()
            } else {
                // FIX 2: Actually place the order instead of just showing the dialog
                placeOrder()
            }
        }

        binding.buttonBacks.setOnClickListener {
            finish()
        }
    }

    private fun placeOrder() {
        userId = auth.currentUser?.uid ?: ""
        val time = System.currentTimeMillis()
        val itemPushKey = databaseReference.child("OrderDetails").push().key

        val orderDetails = OrderDetails(
            userId,
            name,
            foodName,
            foodPrice,
            foodImage,
            foodQuantity,
            address,
            totalAmount,
            phone,
            false,
            false,
            itemPushKey,
            time
        )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails).addOnSuccessListener {

            // --- ADD THIS BLOCK TO SAVE TO HISTORY ---
            val buyHistoryRef = databaseReference.child("users").child(userId).child("BuyHistory").child(itemPushKey)
            buyHistoryRef.setValue(orderDetails)
            // -----------------------------------------

            removeItemFromCart()
            val bottomSheetDialogue = CongratesBottomSheet()
            bottomSheetDialogue.show(supportFragmentManager, "Test")

        }.addOnFailureListener {
            Toast.makeText(this, "Failed to Order", Toast.LENGTH_SHORT).show()
        }
    }


    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("users").child(userId).child("OrderHistory").push().setValue(orderDetails)
            .addOnSuccessListener {
                Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to Place Order", Toast.LENGTH_SHORT).show()
            }

    }

    private fun removeItemFromCart() {
        val cartItemsReference = databaseReference.child("users").child(userId).child("CartItems")
        cartItemsReference.removeValue()
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodPrice.size) {
            var price = foodPrice[i]

            // Remove '$' symbol if present
            if (price.startsWith("$")) {
                price = price.substring(1)
            }
            if (price.endsWith("$")) {
                price = price.dropLast(1)
            }

            // Parse price safely
            val priceIntValue = try {
                price.trim().toInt()
            } catch (e: NumberFormatException) {
                0
            }

            // Safety check for index bounds
            if (i < foodQuantity.size) {
                val quantity = foodQuantity[i]
                totalAmount += priceIntValue * quantity
            }
        }
        return totalAmount
    }


    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            // Use databaseReference, not 'database' variable which wasn't defined in your snippet correctly
            val useReference = databaseReference.child("users").child(userId)

            useReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val names = snapshot.child("name").getValue(String::class.java) ?: ""
                        val phones = snapshot.child("phone").getValue(String::class.java) ?: ""
                        val addresses = snapshot.child("address").getValue(String::class.java) ?: ""

                        binding.apply {
                            name.setText(names)
                            phone.setText(phones)
                            address.setText(addresses)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors here
                }
            })
        }
    }
}
