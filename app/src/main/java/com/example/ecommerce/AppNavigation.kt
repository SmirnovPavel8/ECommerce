package com.example.ecommerce

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ecommerce.pages.CategoryProductsPage
import com.example.ecommerce.pages.ProductDetailsPage
import com.example.ecommerce.screen.AuthScreen
import com.example.ecommerce.screen.HomeScreen
import com.example.ecommerce.screen.LoginScreen
import com.example.ecommerce.screen.SignupScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun AppNavigation(modifier: Modifier=Modifier){

    val navController=rememberNavController()
    GlobalNavigation.navController=navController
    val isLogeedIn=Firebase.auth.currentUser!=null
    val firstPage=if(isLogeedIn) "home" else "auth"

   NavHost(navController=navController , startDestination = firstPage) {
       composable("auth"){
           AuthScreen(modifier,navController)

       }
       composable("login"){
           LoginScreen(modifier,navController)

       }
       composable("signup"){
           SignupScreen(modifier,navController)

       }
       composable("home"){
           HomeScreen(modifier,navController)
       }
       composable("category-products/{categoryId}"){
           var categoryId=it.arguments?.getString("categoryId")
           CategoryProductsPage(modifier,categoryId?:"")
       }
       composable("product-details/{productId}"){
           var productId=it.arguments?.getString("productId")
           ProductDetailsPage(modifier,productId?:"")
       }
   }


}
object GlobalNavigation{
    lateinit var navController: NavHostController
}