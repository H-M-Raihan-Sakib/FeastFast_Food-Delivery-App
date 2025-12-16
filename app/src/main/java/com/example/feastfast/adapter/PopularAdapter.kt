package com.example.feastfast.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.feastfast.DetailsActivity
import com.example.feastfast.databinding.PopularItemBinding
import com.example.feastfast.model.MenuItem

class PopularAdapter(
    private val menuItems: List<MenuItem>,
    private val requireContext: Context
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        return PopularViewHolder(
            PopularItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val menuItem = menuItems[position]
        holder.bind(menuItem)

        holder.itemView.setOnClickListener {
            val intent = Intent(requireContext, DetailsActivity::class.java).apply {
                putExtra("MenuItemName", menuItem.foodName)
                putExtra("MenuItemImage", menuItem.foodImage)
                putExtra("MenuItemDescription", menuItem.foodDescription)
                putExtra("MenuItemIngredients", menuItem.foodIngredient)
                putExtra("MenuItemPrice", menuItem.foodPrice)
            }
            requireContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    inner class PopularViewHolder(val binding: PopularItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(menuItem: MenuItem) {
            binding.foodNamePopular.text = menuItem.foodName
            binding.pricePopular.text = menuItem.foodPrice
            binding.ratingBarPopular.rating = menuItem.averageRating // This sets the stars

            val uriString = menuItem.foodImage
            if (!uriString.isNullOrEmpty()) {
                val uri = Uri.parse(uriString)
                Glide.with(requireContext).load(uri).into(binding.imageView6)
            }
        }
    }
}
