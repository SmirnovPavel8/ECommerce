package com.example.ecommerce.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.components.CartItemView
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import com.example.ecommerce.model.ProductModel

@Composable
fun CartPage(modifier: Modifier = Modifier) {
    var userModel = remember { mutableStateOf(UserModel()) }
    val totalPrice = remember { mutableStateOf(0.0) } // Добавляем состояние для общей суммы

    LaunchedEffect(key1 = Unit) {
        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .get().addOnCompleteListener() {
                if (it.isSuccessful) {
                    var result = it.result.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result
                        // Рассчитываем сумму после загрузки данных
                        calculateTotalPrice(result.cartItems) { sum ->
                            totalPrice.value = sum
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
            text = "Your cart",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        // Существующий список товаров (без изменений)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(userModel.value.cartItems.toList()) { (productId, qty) ->
                Row {
                    CartItemView(
                        productId = productId,
                        modifier = Modifier.weight(1f),
                        qty = qty
                    )
                }
            }
        }

        // ▼▼▼ ДОБАВЛЯЕМ ЭТОТ БЛОК ▼▼▼
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            // Строка с общей суммой
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total:",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    "₽${"%.2f".format(totalPrice.value)}", // Форматируем с двумя знаками после запятой
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Существующая кнопка (без изменений)
            Button(
                onClick = { /* TODO: Добавить навигацию на экран оформления */ },
                modifier = Modifier.fillMaxWidth(),
                enabled = userModel.value.cartItems.isNotEmpty()
            ) {
                Text("Proceed to Checkout")
            }
        }
        // ▲▲▲ КОНЕЦ ДОБАВЛЕНИЯ ▲▲▲
    }
}

// ▼▼▼ ДОБАВЛЯЕМ ЭТУ ФУНКЦИЮ ▼▼▼
private fun calculateTotalPrice(
    cartItems: Map<String, Long>,
    callback: (Double) -> Unit
) {
    if (cartItems.isEmpty()) {
        callback(0.0)
        return
    }

    val productsRef = Firebase.firestore.collection("data/stock/products")
    var total = 0.0
    var processedItems = 0

    cartItems.forEach { (productId, qty) ->
        productsRef.document(productId).get()
            .addOnSuccessListener { document ->
                document.toObject(ProductModel::class.java)?.let { product ->
                    total += product.actualPrice.toInt() * qty.toInt()
                }
                processedItems++
                if (processedItems == cartItems.size) {
                    callback(total)
                }
            }
            .addOnFailureListener {
                processedItems++
                if (processedItems == cartItems.size) {
                    callback(total)
                }
            }
    }
}