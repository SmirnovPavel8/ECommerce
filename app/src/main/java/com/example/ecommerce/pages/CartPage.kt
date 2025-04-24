package com.example.ecommerce.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun CartPage(modifier: Modifier=Modifier){
    var userModel= remember {
        mutableStateOf(UserModel())
    }
    LaunchedEffect(key1=Unit) {
        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .get().addOnCompleteListener(){
                if(it.isSuccessful){
                    var result=it.result.toObject(UserModel::class.java)
                    if(result!=null){
                        userModel.value=result
                    }
                }
            }
    }
    Column(
        modifier=modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Text(text="Your cart", style = TextStyle(
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        ))
        LazyColumn {
            items(userModel.value.cartItems.toList()){(productId,qty)->
            Text(text=productId+" ->>>>>>>"+qty)}
        }
    }
}