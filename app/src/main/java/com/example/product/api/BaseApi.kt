package com.example.product.api

import com.google.gson.Gson
import com.google.gson.annotations.Expose
import kotlin.reflect.KClass


abstract class BaseApi {

    abstract fun getApiPath(): String

    abstract fun getResponseModel(): KClass<out ApiResponse>

    enum class ContentType {
        EMPTY,
        JSON,
        FROM
    }

    abstract fun getContentType(): ContentType


    abstract class ApiRequest {
        fun toJson(): String = Gson().toJson(this)
    }

    abstract class ApiResponse {

        @Expose
        var rawBodyString: String? = null

    }
}