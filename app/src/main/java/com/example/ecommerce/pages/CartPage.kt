package com.example.ecommerce.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecommerce.AppUtil
import com.example.ecommerce.GlobalNavigation
import com.example.ecommerce.components.CartItemView
import com.example.ecommerce.components.ProductItemView
import com.example.ecommerce.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@Composable
fun CartPage(modifier: Modifier=Modifier){
    var userModel= remember {
        mutableStateOf(UserModel())
    }
    DisposableEffect(key1=Unit) {
       var listener=Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid!!)
            .addSnapshotListener{it,_ ->

                if(it!=null){
                    var result=it.toObject(UserModel::class.java)
                    if(result!=null){
                        userModel.value=result
                    }
                }
            }
        onDispose {
            listener.remove()
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
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(userModel.value.cartItems.toList(),key={it.first}) { (productId, qty) ->
                Row {

                    CartItemView(productId = productId, modifier = Modifier.weight(1f), qty = qty)


                }
            }
        }
        Button(onClick = {
            GlobalNavigation.navController.navigate("checkout")
        },
            modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text(text="Add to cart", fontSize = 16.sp)
        }
    }
}