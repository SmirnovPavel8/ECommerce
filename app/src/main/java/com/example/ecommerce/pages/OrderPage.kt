package com.example.ecommerce.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.GlobalNavigation
import com.example.ecommerce.components.OrderItemView
import com.example.ecommerce.model.OrderModel
import com.example.ecommerce.model.ProductModel
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderPage(
    modifier: Modifier = Modifier,
    orderId: String
) {
    val order = remember { mutableStateOf<OrderModel?>(null) }
    val user = remember { mutableStateOf<UserModel?>(null) }
    val products = remember { mutableStateListOf<ProductModel>() }
    val totalPrice = remember { mutableStateOf(0.0) }
    val isLoading = remember { mutableStateOf(true) }
    val totalItemsCount = remember { mutableStateOf(0) } // Добавляем счетчик общего количества товаров

    LaunchedEffect(orderId) {
        Firebase.firestore.collection("orders")
            .document(orderId)
            .get()
            .addOnSuccessListener { document ->
                val loadedOrder = document.toObject(OrderModel::class.java)?.apply {
                    uid = document.id
                }
                order.value = loadedOrder

                loadedOrder?.let { currentOrder ->
                    // Рассчитываем общее количество товаров
                    totalItemsCount.value = currentOrder.cartItems.values.sum().toInt()

                    Firebase.firestore.collection("users")
                        .document(currentOrder.userId)
                        .addSnapshotListener { snapshot, _ ->
                            snapshot?.toObject(UserModel::class.java)?.let {
                                user.value = it
                            }
                        }

                    if (currentOrder.cartItems.isNotEmpty()) {
                        loadOrderProducts(
                            currentOrder.cartItems,
                            onProductsLoaded = { loadedProducts ->
                                products.clear()
                                products.addAll(loadedProducts)
                                calculateOrderTotal(loadedProducts, currentOrder.cartItems) { total ->
                                    totalPrice.value = total
                                }
                                isLoading.value = false
                            }
                        )
                    } else {
                        isLoading.value = false
                    }
                } ?: run {
                    isLoading.value = false
                }
            }
            .addOnFailureListener {
                isLoading.value = false
            }
    }

    when {
        isLoading.value -> FullScreenLoader()
        order.value == null -> OrderNotFound()
        else -> OrderDetailsContent(
            order = order.value!!,
            user = user.value,
            products = products,
            totalPrice = totalPrice.value,
            totalItemsCount = totalItemsCount.value // Передаем общее количество
        )
    }
}

@Composable
private fun OrderDetailsContent(
    order: OrderModel,
    user: UserModel?,
    products: List<ProductModel>,
    totalPrice: Double,
    totalItemsCount: Int // Добавляем параметр
) {
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember { dateFormat.format(Date(order.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Шапка заказа
        Text(
            text = "Заказ #${order.uid.take(8)}",
            style = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(text = "Дата: $formattedDate")

        Spacer(modifier = Modifier.height(16.dp))

        // Информация о покупателе
        user?.let {
            Text(
                text = "Данные покупателя:",
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Имя: ${it.name}")
            Text(text="Почта: ${it.email}")
            Text(text="Телефон: ${it.number}")
            Text(text = "Адрес: ${it.addres}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Список товаров - теперь показываем общее количество
        Text(
            text = "Товары (${totalItemsCount} шт.):", // Используем totalItemsCount вместо cartItems.size
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(products) { product ->
                val quantity = order.cartItems[product.id] ?: 0
                OrderItemView(
                    product = product,
                    quantity = quantity.toInt(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Итоговая сумма
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Итого:")
            Text(
                text = "%.2f ₽".format(totalPrice),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка возврата
        Button(
            onClick = { GlobalNavigation.navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Назад к заказам", fontSize = 16.sp)
        }
    }
}


// Загрузка товаров заказа
private fun loadOrderProducts(
    cartItems: Map<String, Long>,
    onProductsLoaded: (List<ProductModel>) -> Unit
) {
    val productsRef = Firebase.firestore.collection("data/stock/products")
    val productsList = mutableListOf<ProductModel>()
    var processedItems = 0

    cartItems.forEach { (productId, _) ->
        productsRef.document(productId).get()
            .addOnSuccessListener { document ->
                document.toObject(ProductModel::class.java)?.let {
                    productsList.add(it)
                }
                processedItems++
                if (processedItems == cartItems.size) {
                    onProductsLoaded(productsList)
                }
            }
            .addOnFailureListener {
                processedItems++
                if (processedItems == cartItems.size) {
                    onProductsLoaded(productsList)
                }
            }
    }
}

// Расчет общей суммы заказа
private fun calculateOrderTotal(
    products: List<ProductModel>,
    cartItems: Map<String, Long>,
    callback: (Double) -> Unit
) {
    var total = 0.0
    products.forEach { product ->
        val quantity = cartItems[product.id]?.toInt() ?: 0
        total += product.actualPrice.toDouble() * quantity
    }
    callback(total)
}

@Composable
private fun FullScreenLoader() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun OrderNotFound() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Заказ не найден")
    }
}