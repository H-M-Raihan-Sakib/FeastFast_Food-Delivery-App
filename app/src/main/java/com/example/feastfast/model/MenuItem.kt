package com.example.feastfast.model

data class MenuItem(
    val foodName: String? = null,
    val foodPrice: String? = null,
    val foodDescription: String? = null,
    val foodImage: String? = null,
    val foodIngredient: String? = null,
    var averageRating: Float = 0f // This will hold the calculated average
)
