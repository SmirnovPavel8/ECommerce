package com.example.ecommerce.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.components.ProductItemView
import com.example.ecommerce.model.ProductModel
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@Composable
fun FavoritePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val userData = remember { mutableStateOf<UserModel?>(null) }
    val favoriteProducts = remember { mutableStateListOf<ProductModel>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            Firebase.firestore.collection("users")
                .document(uid)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.let { document ->
                        val favorites = document.get("favoriteItems") as? Map<String, Boolean> ?: emptyMap()
                        val favoriteIds = favorites.filter { it.value }.keys.toList()

                        if (favoriteIds.isNotEmpty()) {
                            loadFavoriteProducts(favoriteIds) { products ->
                                favoriteProducts.clear()
                                favoriteProducts.addAll(products)
                                isLoading.value = false
                            }
                        } else {
                            favoriteProducts.clear()
                            isLoading.value = false
                        }
                    }
                }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Избранные товары",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            favoriteProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Нет избранных товаров")
                }
            }
            else -> {
                LazyColumn {
                    items(favoriteProducts.chunked(2)) { rowItems ->
                        Row {
                            rowItems.forEach { product ->
                                ProductItemView(
                                    product = product,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun loadFavoriteProducts(
    productIds: List<String>,
    callback: (List<ProductModel>) -> Unit
) {
    if (productIds.isEmpty()) {
        callback(emptyList())
        return
    }

    Firebase.firestore.collection("data/stock/products")
        .whereIn("id", productIds)
        .get()
        .addOnSuccessListener { snapshot ->
            val products = snapshot.documents.mapNotNull { it.toObject(ProductModel::class.java) }
            callback(products)
        }
        .addOnFailureListener {
            callback(emptyList())
        }
}