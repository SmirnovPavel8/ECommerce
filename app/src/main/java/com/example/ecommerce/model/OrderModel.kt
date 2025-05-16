package com.example.ecommerce.model

data class OrderModel(
    var uid: String = "", // ID заказа
    val userId: String = "",
    var cartItems: Map<String, Long> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis() // Добавим timestamp для сортировки
)