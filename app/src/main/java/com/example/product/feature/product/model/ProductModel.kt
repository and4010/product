package com.example.product.feature.product.model

data class ProductModel(
    val price : Int,
    val martShortName : String,
    val imageUrl : String,
    val finalPrice : Int,
    val martName : String,
    val stockAvailable : Int,
    val martId : Int,
)


fun ProductModel.toProductItem(): ProductItem {
    return ProductItem(
        price = price,
        martShortName = martShortName,
        imageUrl = imageUrl,
        finalPrice = finalPrice,
        martName = martName,
        stockAvailable = stockAvailable,
        martId = martId
    )
}