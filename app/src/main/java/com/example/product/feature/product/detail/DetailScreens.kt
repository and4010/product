package com.example.product.feature.product.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.product.R
import com.example.product.feature.product.detail.DetailViewModel.DetailIntent
import com.example.product.feature.product.detail.DetailViewModel.DetailState

@Composable
fun DetailScreen(onBack : () -> Unit,viewModel: DetailViewModel = hiltViewModel()){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DetailContent(state, onIntent = {
        when(it){
            is DetailIntent.OnBack -> onBack()
        }
    })
}


@Composable
fun DetailContent(state: DetailState, onIntent: (DetailIntent) -> Unit = {}){
    Column{
        ToolBar(onIntent = onIntent)
        Body(state)
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolBar(onIntent: (DetailIntent) -> Unit = {}){
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "商品資訊",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = { onIntent(DetailIntent.OnBack) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
    )
}

@Composable
private fun Body(state: DetailState){
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .placeholder(R.drawable.baseline_error_24)
                .data(state.imageUrl)
                .size(Size.ORIGINAL) // Set the target size to load the image at.
                .build(), contentDescription = "照片"
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "商品編號", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = state.martId.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text =  state.martName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$" + state.finalPrice,
                color = Color(0xFFE91E63),fontSize = 16.sp,
                fontWeight = FontWeight.Bold)
        }

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

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun DetailScreenPreview() {
    DetailContent(state = DetailState())
}