package com.example.feastfast.model

import java.io.Serializable

data class OrderDetails(
    val userUid: String? = null,
    val userName: String? = null,

    // RENAMED to match Firebase keys exactly (Singular)
    val foodName: MutableList<String>? = null,
    val foodPrice: MutableList<String>? = null,
    val foodImage: MutableList<String>? = null,
    val foodQuantity: MutableList<Int>? = null,

    val address: String? = null,
    val totalPrice: String? = null,
    val phoneNumber: String? = null,
    val orderAccepted: Boolean = false,
    val paymentReceived: Boolean = false,
    val itemPushKey: String? = null,
    val currentTime: Long = 0,
    val orderDispatch: Boolean = false
) : Serializable
