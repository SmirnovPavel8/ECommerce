package com.example.ecommerce.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ecommerce.model.OrderModel
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun AllOrdersPage(
    modifier: Modifier = Modifier,

) {
    val orders = remember { mutableStateListOf<OrderModel>() }
    val users = remember { mutableStateMapOf<String, UserModel>() }
    val isLoading = remember { mutableStateOf(true) }

    LaunchedEffect(key1 = Unit) {
        Firebase.firestore.collection("orders")
            .get()
            .addOnSuccessListener { ordersSnapshot ->
                val ordersList = ordersSnapshot.toObjects(OrderModel::class.java)
                ordersList.forEach { order ->
                    order.uid = ordersSnapshot.documents[ordersList.indexOf(order)].id
                }
                orders.clear()
                orders.addAll(ordersList)

                // Загружаем данные пользователей
                val userIds = orders.map { it.userId }.distinct()
                if (userIds.isNotEmpty()) {
                    Firebase.firestore.collection("users")
                        .whereIn("uid", userIds)
                        .get()
                        .addOnSuccessListener { usersSnapshot ->
                            usersSnapshot.forEach { doc ->
                                val user = doc.toObject(UserModel::class.java)
                                users[user.uid] = user
                            }
                            isLoading.value = false
                        }
                } else {
                    isLoading.value = false
                }
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Мои заказы",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (orders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("У вас пока нет заказов")
            }
        } else {
            LazyColumn {
                items(orders) { order ->
                    val user = users[order.userId]
                    OrderCard(order = order, user = user)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderModel, user: UserModel?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Заказ #${order.uid.take(8)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Товаров: ${order.cartItems.size}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            user?.let {
                Text(text = "Покупатель: ${it.name}")
                Text(text = "Адрес: ${it.addres}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* Действие при нажатии на кнопку */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Детали заказа")
            }
        }
    }
}