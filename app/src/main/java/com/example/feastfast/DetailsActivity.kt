package com.example.feastfast

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.feastfast.databinding.ActivityDetailsBinding
import com.example.feastfast.model.CartItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private  var foodName: String?=null
    private  var foodImage: String?=null
    private  var foodDescription: String?=null
    private  var foodIngredient: String?=null
    private  var foodPrice: String?=null
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        foodName = intent.getStringExtra("MenuItemName")
        foodDescription = intent.getStringExtra("MenuItemDescription")
        foodIngredient = intent.getStringExtra("MenuItemIngredients")
        foodPrice = intent.getStringExtra("MenuItemPrice")
        foodImage = intent.getStringExtra("MenuItemImage")

        with(binding) {
            detailFoodName.text = foodName
            DescriptionTextView.text = foodDescription
            IngredienTextView.text = foodIngredient
            Glide.with(this@DetailsActivity).load(Uri.parse(foodImage)).into(DetailFoodImage)


        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }




        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.AddItemButton.setOnClickListener {
            addItemtoCart()
            finish()
        }
    }

    private fun addItemtoCart() {
        val database: DatabaseReference = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid ?: ""

        // Create a CartItem object
        val uriString = foodImage ?: "" // Provide a default empty string if null

        val cartItem = CartItem(
            foodName.toString(),
            foodPrice.toString(),
            foodDescription.toString(),
            uriString, // Use the safe variable
            1,
            foodIngredient.toString()
        )

        // Save data to: users -> userId -> CartItems -> foodName
        database.child("users").child(userId).child("CartItems").child(foodName.toString()).setValue(cartItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Item Added to Cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add item to cart", Toast.LENGTH_SHORT).show()
            }
    }

}
