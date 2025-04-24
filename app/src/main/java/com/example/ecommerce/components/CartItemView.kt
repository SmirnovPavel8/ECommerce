package com.example.ecommerce.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ecommerce.AppUtil
import com.example.ecommerce.GlobalNavigation
import com.example.ecommerce.model.ProductModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun CartItemView(modifier: Modifier = Modifier, productId: String, qty: Long) {
    val productsList = remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("data")
            .document("stock")
            .collection("products")
            .whereEqualTo("id", productId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    productsList.value = task.result.documents.mapNotNull {
                        it.toObject(ProductModel::class.java)
                    }
                }
            }
    }

    productsList.value.firstOrNull()?.let { product ->
        Card(
            modifier = modifier.padding(8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Изображение товара (слева)
                AsyncImage(
                    model = product.images.firstOrNull(),
                    contentDescription = product.title,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            GlobalNavigation.navController.navigate("product-details/${product.id}")
                        },
                    contentScale = ContentScale.Crop
                )

                // Название и цена (по центру)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = product.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "₽${product.price}",
                            fontSize = 14.sp,
                            style = TextStyle(textDecoration = TextDecoration.LineThrough)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₽${product.actualPrice}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Кнопки управления (справа)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Кнопка удаления
                    IconButton(
                        onClick = { AppUtil.removeItemCompletely(product.id, context) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    // Блок количества
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { AppUtil.removeItemFromCart(product.id, context) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(text = "-", fontSize = 16.sp)
                        }

                        Text(
                            text = qty.toString(),
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )

                        IconButton(
                            onClick = { AppUtil.addItemToCart(product.id, context) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text(text = "+", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}
