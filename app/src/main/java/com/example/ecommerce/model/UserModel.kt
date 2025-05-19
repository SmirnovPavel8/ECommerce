package com.example.ecommerce.model

data class UserModel(
    val name: String = "",
    val email: String = "",
    val uid: String = "",
    var cartItems: Map<String, Long> = emptyMap(),
    var favoriteItems: Map<String, Boolean> = emptyMap(), // Добавляем поле для избранного
    val addres: String = "",
    val role: String = "",
    val number: String = ""
)