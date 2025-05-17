package com.example.ecommerce.pages

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ecommerce.GlobalNavigation.navController
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePage(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentUser = Firebase.auth.currentUser
    val userData = remember { mutableStateOf<UserModel?>(null) }

    // Загружаем данные пользователя
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            Firebase.firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userData.value = document.toObject(UserModel::class.java)
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        navController.navigate("auth") {
                            popUpTo(0)
                        }
                        Toast.makeText(context, "Вы вышли из системы", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выход")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            userData.value?.let { user ->
                // Основная информация
                ProfileItem("Имя", user.name)
                ProfileItem("Email", user.email)
                ProfileItem("Телефон", user.number)
                ProfileItem("Адрес", user.addres)

                // Роль (если нужно показывать)
                if (user.role.isNotEmpty()) {
                    ProfileItem("Роль", user.role)
                }
            } ?: run {
                // Показываем загрузку, если данные ещё не загружены
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ProfileItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value.ifEmpty { "Не указано" },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp)
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}