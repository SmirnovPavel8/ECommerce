package com.example.ecommerce.pages

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ecommerce.GlobalNavigation.navController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier=Modifier){
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Главная") },
                actions = {
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("auth") {
                            popUpTo(0) // Очистка стека навигации
                        }
                        Toast.makeText(context, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выход")
                    }
                }
            )
        }
    ){}
}