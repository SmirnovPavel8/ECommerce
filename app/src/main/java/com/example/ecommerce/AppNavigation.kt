package com.example.ecommerce

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecommerce.pages.AllOrdersPage
import com.example.ecommerce.pages.CategoryProductsPage
import com.example.ecommerce.pages.CheckoutPage
import com.example.ecommerce.pages.OrderPage
import com.example.ecommerce.pages.ProductDetailsPage
import com.example.ecommerce.screen.AuthScreen
import com.example.ecommerce.screen.HomeScreen
import com.example.ecommerce.screen.LoginScreen
import com.example.ecommerce.screen.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    GlobalNavigation.navController = navController
    val isLogeedIn = Firebase.auth.currentUser != null
    val firstPage = if(isLogeedIn) "home" else "auth"

    NavHost(navController = navController, startDestination = firstPage) {
        composable("auth") {
            AuthScreen(modifier,navController)
        }
        composable("login") {
            LoginScreen(modifier,navController)
        }
        composable("signup") {
            SignupScreen(modifier,navController)
        }
        composable("home") {
            HomeScreen(modifier,navController)
        }
        composable("category-products/{categoryId}") {
            val categoryId = it.arguments?.getString("categoryId")
            CategoryProductsPage(modifier, categoryId ?: "")
        }
        composable("product-details/{productId}") {
            val productId = it.arguments?.getString("productId")
            ProductDetailsPage(modifier, productId ?: "")
        }
        composable("checkout") {
            CheckoutPage(modifier,navController)
        }
        // Добавляем новый маршрут для страницы заказов
        composable("orders") {
            AllOrdersPage(modifier)
        }
        // Добавляем маршрут для деталей заказа
        composable("order-details/{orderId}") {
            val orderId = it.arguments?.getString("orderId")
            OrderPage(modifier, orderId ?: "")
        }
    }
}
object GlobalNavigation{
    lateinit var navController: NavHostController
}