package com.example.feastfast.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.feastfast.R
import com.example.feastfast.databinding.BuyAgainItemBinding
import com.example.feastfast.model.Rating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BuyAgainAdapter(
    private val buyAgainFoodName: ArrayList<String>,
    private val buyAgainFoodPrice: ArrayList<String>,
    private val buyAgainFoodImage: ArrayList<String>,
    private val context: Context
) : RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BuyAgainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BuyAgainViewHolder, position: Int) {
        holder.bind(
            buyAgainFoodName[position],
            buyAgainFoodPrice[position],
            buyAgainFoodImage[position]
        )
    }

    override fun getItemCount(): Int = buyAgainFoodName.size

    inner class BuyAgainViewHolder(private val binding: BuyAgainItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(foodName: String, foodPrice: String, foodImage: String) {
            binding.buyAgainFoodName.text = foodName
            binding.buyAgainFoodPrice.text = foodPrice

            val uri = Uri.parse(foodImage)
            Glide.with(context).load(uri).into(binding.buyAgainFoodImage)

            binding.rateButton.setOnClickListener {
                showRatingDialog(foodName)
            }
        }

        private fun showRatingDialog(foodName: String) {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null)
            val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)

            AlertDialog.Builder(context)
                .setTitle("Rate $foodName")
                .setView(dialogView)
                .setPositiveButton("Submit") { dialog, _ ->
                    val rating = ratingBar.rating
                    submitRating(foodName, rating)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        private fun submitRating(foodName: String, ratingValue: Float) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val database = FirebaseDatabase.getInstance().reference

            val rating = Rating(userId, foodName, ratingValue)

            database.child("ratings").child(foodName).child(userId).setValue(rating)
                .addOnSuccessListener {
                    Toast.makeText(context, "Thank you for your review!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to submit rating.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
