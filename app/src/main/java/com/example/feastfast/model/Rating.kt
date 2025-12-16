package com.example.feastfast.model

data class Rating(
    val userId: String? = null,
    val foodName: String? = null,
    val ratingValue: Float = 0f
) {
    // Empty constructor is required by Firebase for deserialization
    constructor() : this("", "", 0f)
}
