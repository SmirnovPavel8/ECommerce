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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var product by remember{
        mutableStateOf(ProductModel())
    }
    LaunchedEffect(key1=Unit) {
        Firebase.firestore.collection("data")
            .document("stock").collection("products")
            .document(productId).get().addOnCompleteListener(){
                if(it.isSuccessful){
                    val reuslt=it.result.toObject(ProductModel::class.java)
                    if(reuslt!=null){
                        product=reuslt
                    }
                }
            }
    }
    var context= LocalContext.current
    Card(
        modifier=modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape= RoundedCornerShape(12.dp),
        colors=CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .clickable {
                    GlobalNavigation.navController.navigate("product-details/"+product.id)
                },
            verticalAlignment = Alignment.CenterVertically

        ){
            AsyncImage(
                model=product.images.firstOrNull(),
                contentDescription = product.title,
                modifier=Modifier.height(120.dp)
                    .width(100.dp)
            )
            Column(modifier=Modifier.padding(8.dp)
                .weight(1f)){
                Text(
                    text=product.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
                Text(text="₽"+ product.actualPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ){
                    IconButton(onClick={
                        AppUtil.removeFromCart(productId,context)
                    }) {
                        Text(text="-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text="$qty", fontSize = 16.sp)
                    IconButton(onClick={
                        AppUtil.addItemToCart(productId,context)
                    }) {
                        Text(text="+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            IconButton(onClick = {
                AppUtil.removeFromCart(productId,context, removeAll = true)
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove from cart"
                )
            }
        }
    }
}
