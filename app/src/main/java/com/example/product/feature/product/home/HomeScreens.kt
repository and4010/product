package com.example.product.feature.product.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.product.R
import com.example.product.feature.product.home.HomeViewModel.HomeIntent
import com.example.product.feature.product.home.HomeViewModel.ProductState
import com.example.product.feature.product.model.ProductItem
import com.example.product.navigation.Screen

@Composable
fun HomeScreen(onNavigate: (Screen) -> Unit,viewModel: HomeViewModel = hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(state = state, onIntent = {
        when(it){
            is HomeIntent.NextPage -> onNavigate(Screen.Detail(it.id))
            is HomeIntent.FilterText -> Unit
        }
        viewModel.onIntent(it)
    })
}


@Composable
private fun HomeContent(state: ProductState,onIntent: (HomeIntent) -> Unit = {}) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        SearchInputField(
            state = state,
            onIntent = onIntent,
        )
        ProductList(state = state,onIntent = onIntent)
    }

}


@Composable
private fun SearchInputField(
    state: ProductState,
    onIntent: (HomeIntent) -> Unit = {}
) {

    TextField(
        value = state.filterText,
        onValueChange = {
            onIntent(HomeIntent.FilterText(it))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon"
            )
        },
        placeholder = {
            Text(text = "搜尋")
        },
        singleLine = true,
        shape = RoundedCornerShape(50), // 圓形邊框
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp), // 高度設定為 Material 標準
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}


@Composable
private fun ProductList(state: ProductState, onIntent: (HomeIntent) -> Unit = {}) {
    Column(
        modifier = Modifier.padding(top = 10.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ){
            items(
                state.data, key = {it.martId}
            ) { item ->
                ProductItem(item = item,onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun ProductItem(item: ProductItem, onIntent: (HomeIntent) -> Unit = {}) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .clickable{
                onIntent(HomeIntent.NextPage(item.martId))
            }
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Column (
            modifier = Modifier.height(150.dp).width(150.dp)){
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .placeholder(R.drawable.baseline_error_24)
                    .data(item.imageUrl)
                    .size(Size.ORIGINAL) // Set the target size to load the image at.
                    .build(), contentDescription = "照片"
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            Text(text = item.martName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = item.finalPrice.toString(),
                color = Color(0xFFE91E63), fontSize = 18.sp,
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End)
            ) {
                Icon(painter = painterResource(R.drawable.baseline_favorite_24), contentDescription = "我的最愛")
                Icon(painter = painterResource(R.drawable.baseline_shopping_cart_24), contentDescription = "購物車")
            }
        }
    }
}





@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    HomeContent(state = ProductState())
}
