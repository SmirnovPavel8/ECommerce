package com.example.ecommerce.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.GlobalNavigation
import com.example.ecommerce.model.OrderModel
import com.example.ecommerce.model.ProductModel
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AllOrdersPage(
    modifier: Modifier = Modifier
) {
    val orders = remember { mutableStateListOf<OrderModel>() }
    val users = remember { mutableStateMapOf<String, UserModel>() }
    val isLoading = remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val ordersQuery = Firebase.firestore.collection("orders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

        val listener = ordersQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                isLoading.value = false
                return@addSnapshotListener
            }

            snapshot?.let { ordersSnapshot ->
                val ordersList = ordersSnapshot.toObjects(OrderModel::class.java)
                ordersList.forEachIndexed { index, order ->
                    order.uid = ordersSnapshot.documents[index].id
                }

                orders.clear()
                orders.addAll(ordersList)

                // Загружаем данные пользователей только если есть заказы
                if (ordersList.isNotEmpty()) {
                    val userIds = ordersList.map { it.userId }.distinct()
                    Firebase.firestore.collection("users")
                        .whereIn("uid", userIds)
                        .get()
                        .addOnSuccessListener { usersSnapshot ->
                            users.clear()
                            usersSnapshot.forEach { doc ->
                                users[doc.id] = doc.toObject(UserModel::class.java)
                            }
                            isLoading.value = false
                        }
                        .addOnFailureListener {
                            isLoading.value = false
                        }
                } else {
                    isLoading.value = false
                }
            }
        }

        onDispose {
            listener.remove()
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Все заказы",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading.value -> FullScreenLoader()
            orders.isEmpty() -> EmptyOrdersMessage()
            else -> OrdersList(orders, users)
        }
    }
}

@Composable
private fun OrdersList(
    orders: List<OrderModel>,
    users: Map<String, UserModel>
) {
    LazyColumn {
        items(orders) { order ->
            OrderCard(
                order = order,
                user = users[order.userId],
                onOrderClick = {
                    GlobalNavigation.navController.navigate("order-details/${order.uid}")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun OrderCard(
    order: OrderModel,
    user: UserModel?,
    onOrderClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(order.timestamp) {
        dateFormat.format(Date(order.timestamp))
    }
    val orderTotal = remember { mutableStateOf("...") }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val isLoadingDelete = remember { mutableStateOf(false) }

    // Расчет суммы заказа
    LaunchedEffect(order) {
        orderTotal.value = withContext(Dispatchers.IO) {
            calculateOrderTotal(order)
        }
    }

    // Диалог подтверждения удаления
    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Подтверждение удаления") },
            text = { Text("Вы уверены, что хотите удалить этот заказ?") },
            confirmButton = {
                Button(
                    onClick = {
                        isLoadingDelete.value = true
                        Firebase.firestore.collection("orders")
                            .document(order.uid)
                            .delete()
                            .addOnSuccessListener {
                                isLoadingDelete.value = false
                                showDeleteDialog.value = false
                            }
                            .addOnFailureListener {
                                isLoadingDelete.value = false
                                // Можно показать ошибку
                            }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    if (isLoadingDelete.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onError,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Удалить")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

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
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            user?.let {
                Text(text = "Клиент: ${it.name}")
                Text(text = "Email: ${it.email}")
                Text(text = "Телефон: ${it.number}")
                Text(text = "Адрес: ${it.addres}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Товаров: ${order.cartItems.size}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = "Сумма: ${orderTotal.value} ₽",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOrderClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Подробнее")
                }

                Button(
                    onClick = { showDeleteDialog.value = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Удалить")
                }
            }
        }
    }
}

private suspend fun calculateOrderTotal(order: OrderModel): String {
    return try {
        var total = 0.0

        if (order.cartItems.isEmpty()) return "0.00"

        val productIds = order.cartItems.keys.toList()
        val productsSnapshot = Firebase.firestore.collection("data/stock/products")
            .whereIn("id", productIds)
            .get()
            .await()

        order.cartItems.forEach { (productId, quantity) ->
            productsSnapshot.documents.find { it.id == productId }?.let { doc ->
                doc.toObject(ProductModel::class.java)?.let { product ->
                    if (product.actualPrice.isNotEmpty()) {
                        total += product.actualPrice.toDouble() * quantity
                    }
                }
            }
        }

        "%.2f".format(total)
    } catch (e: Exception) {
        "0.00"
    }
}

@Composable
private fun FullScreenLoader() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyOrdersMessage() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Список заказов пуст")
    }
}