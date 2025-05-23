package com.example.product.api


import com.example.product.feature.product.model.ProductModel
import kotlin.reflect.KClass

class Product : BaseApi() {
    override fun getApiPath(): String = "https://m.senao.com.tw/apis2/test/marttest.jsp"

    override fun getResponseModel(): KClass<out ApiResponse> = Response::class

    override fun getContentType(): ContentType = ContentType.EMPTY



    data class Response(
        val data: List<ProductModel>,
    ) : ApiResponse()

}