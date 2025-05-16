package com.example.ecommerce.screen

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ecommerce.model.UserModel
import com.example.ecommerce.pages.AllOrdersPage
import com.example.ecommerce.pages.CartPage
import com.example.ecommerce.pages.CategoryProductsPage
import com.example.ecommerce.pages.FavoritePage
import com.example.ecommerce.pages.HomePage
import com.example.ecommerce.pages.ProfilePage
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.firestore

@Composable
fun HomeScreen(modifier: Modifier = Modifier, navController: NavController) {
    val navItemList = listOf(
        NavItem("Home", Icons.Default.Home),
        NavItem("Favorite", Icons.Default.Favorite),
        NavItem("Cart", Icons.Default.ShoppingCart),
        NavItem("Profile", Icons.Default.Person),
        NavItem("Orders", Icons.Default.DateRange)
    )

    var selectedIndex by rememberSaveable {
        mutableStateOf(0)
    }

    // Получаем текущего пользователя
    val currentUser = Firebase.auth.currentUser
    var userRole by remember { mutableStateOf("") }

    // Загружаем роль пользователя из Firestore
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // Здесь предполагается, что у вас есть коллекция "users" в Firestore
            // с документами, где uid пользователя - это ID документа
            Firebase.firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    userRole = document.getString("role") ?: "customer"
                }
                .addOnFailureListener {
                    userRole = "customer"
                }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItemList.forEachIndexed { index, navItem ->
                    // Скрываем пункт "Orders", если пользователь не работник
                    if (index != 4 || userRole == "worker") {
                        NavigationBarItem(
                            selected = index == selectedIndex,
                            onClick = {
                                selectedIndex = index
                            },
                            icon = {
                                Icon(imageVector = navItem.icon, contentDescription = navItem.label)
                            },
                            label = {
                                Text(text = navItem.label)
                            }
                        )
                    }
                }
            }
        }
    ) {
        // Проверяем роль при попытке перейти на экран заказов
        val actualIndex = if (selectedIndex == 4 && userRole != "worker") {
            0 // перенаправляем на домашний экран
        } else {
            selectedIndex
        }

        ContentScreen(modifier = modifier.padding(it), actualIndex)
    }
}

@Composable
fun ContentScreen(modifier: Modifier = Modifier, selectedIndex: Int) {
    when (selectedIndex) {
        0 -> HomePage(modifier)
        1 -> FavoritePage(modifier)
        2 -> CartPage(modifier)
        3 -> ProfilePage(modifier)
        4 -> AllOrdersPage(modifier)
    }
}
data class NavItem(
    val label:String,
    val icon:ImageVector
)