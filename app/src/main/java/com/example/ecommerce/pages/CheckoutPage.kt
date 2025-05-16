package com.example.ecommerce.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ecommerce.AppUtil
import com.example.ecommerce.model.OrderModel
import com.example.ecommerce.model.ProductModel
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun CheckoutPage(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val userModel = remember { mutableStateOf(UserModel()) }
    val productList = remember { mutableStateListOf<ProductModel>() }
    val subTotal = remember { mutableStateOf(0f) }
    val discount = remember { mutableStateOf(0f) }
    val tax = remember { mutableStateOf(0f) }
    val total = remember { mutableStateOf(0f) }
    val isLoading = remember { mutableStateOf(false) }
    val showSuccessDialog = remember { mutableStateOf(false) }

    fun calculateAndAssign() {
        subTotal.value = 0f // Сбрасываем перед пересчетом
        productList.forEach {
            if (it.actualPrice.isNotEmpty()) {
                val qty = userModel.value.cartItems[it.id] ?: 0
                subTotal.value += it.actualPrice.toFloat() * qty
            }
        }
        discount.value = subTotal.value * (AppUtil.getDiscountPercentage()) / 100
        tax.value = subTotal.value * (AppUtil.getTaxPercentage()) / 100
        total.value = "%.2f".format(subTotal.value - discount.value + tax.value).toFloat()
    }

    fun placeOrder() {
        isLoading.value = true
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            isLoading.value = false
            return
        }

        // Создаем ссылку на новый документ (Firestore сгенерирует uid автоматически)
        val orderRef = Firebase.firestore.collection("orders").document()

        // Создаем модель заказа с uid из сгенерированного ID документа
        val order = OrderModel(
            uid = orderRef.id, // Сохраняем uid в поле документа
            userId = currentUser.uid,
            cartItems = userModel.value.cartItems
        )

        // Одной операцией записываем документ
        orderRef.set(order)
            .addOnSuccessListener {
                // Очищаем корзину пользователя после успешного оформления заказа
                Firebase.firestore.collection("users")
                    .document(currentUser.uid)
                    .update("cartItems", emptyMap<String, Long>())
                    .addOnSuccessListener {
                        isLoading.value = false
                        showSuccessDialog.value = true
                    }
                    .addOnFailureListener { e ->
                        isLoading.value = false
                        // Можно показать сообщение об ошибке очистки корзины
                    }
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                // Здесь можно показать ошибку создания заказа
            }
    }

    LaunchedEffect(key1 = Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return@LaunchedEffect

        Firebase.firestore.collection("users")
            .document(currentUser.uid)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val result = it.result.toObject(UserModel::class.java)
                    if (result != null) {
                        userModel.value = result

                        Firebase.firestore.collection("data")
                            .document("stock").collection("products")
                            .whereIn("id", userModel.value.cartItems.keys.toList())
                            .get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val resultProducts = task.result.toObjects(ProductModel::class.java)
                                    productList.clear()
                                    productList.addAll(resultProducts)
                                    calculateAndAssign()
                                }
                            }
                    }
                }
            }
    }

    if (showSuccessDialog.value) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog.value = false },
            title = { Text("Заказ оформлен") },
            text = { Text("Ваш заказ успешно оформлен!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog.value = false
                        navController.navigate("home")
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    Column(
        modifier = modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Checkout", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Deliver to :", fontWeight = FontWeight.SemiBold)
        Text(text = userModel.value.name)
        Text(text = userModel.value.addres)
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        RowCheckoutItems(title = "Subtotal", value = subTotal.value.toString())
        Spacer(modifier = Modifier.height(16.dp))
        RowCheckoutItems(title = "Discount", value = discount.value.toString())
        Spacer(modifier = Modifier.height(16.dp))
        RowCheckoutItems(title = "Tax (+)", value = tax.value.toString())
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "To pay",
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "$" + total.value.toString(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { placeOrder() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading.value && userModel.value.cartItems.isNotEmpty()
        ) {
            if (isLoading.value) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = "Оформить заказ", color = Color.White)
            }
        }
    }
}

@Composable
fun RowCheckoutItems(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text(text = "₽" + value, fontSize = 18.sp)
    }
}