package com.example.product.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen() {



    @Serializable
    object Home : Screen()
    @Serializable
    data class Detail(val martId: Int) : Screen()



}
