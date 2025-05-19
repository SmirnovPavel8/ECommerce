package com.example.ecommerce.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.components.BannerView
import com.example.ecommerce.components.CategoriesView
import com.example.ecommerce.components.HeaderView
import com.example.ecommerce.components.ProductItemView
import com.example.ecommerce.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun HomePage(modifier: Modifier = Modifier) {
    val productsList = remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    val featuredProducts = remember { mutableStateOf<List<ProductModel>>(emptyList()) }

    // Загружаем все товары
    LaunchedEffect(key1 = Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    productsList.value = task.result.documents.mapNotNull {
                        it.toObject(ProductModel::class.java)
                    }
                    // Для featured можно выбрать первые 6 товаров или по другому критерию
                    featuredProducts.value = productsList.value.take(6)
                }
            }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Шапка
        HeaderView(modifier)

        Spacer(modifier = Modifier.height(10.dp))

        // Баннер
        BannerView(modifier = Modifier.height(150.dp))

        // Категории
        Text(
            text = "Categories",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        CategoriesView()

        // Рекомендуемые товары
        Text(
            text = "Featured Products",
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(top = 16.dp, start = 16.dp)
        )

        // Список товаров (2 в ряд)
        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(featuredProducts.value.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { product ->
                        ProductItemView(
                            product = product,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Добавляем пустое место если нечетное количество
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}