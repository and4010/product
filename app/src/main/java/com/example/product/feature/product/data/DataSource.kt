package com.example.product.feature.product.data

import android.util.Log
import com.example.product.api.ApiManager
import com.example.product.api.ApiManager.call
import com.example.product.api.Product
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DataSource {


    suspend fun callApi() : Flow<Product.Response> = callbackFlow {
        Product().call(callback = object : ApiManager.Callback<Product.Response>{
            override suspend fun isSuccessful(response: Product.Response) {
                trySend(
                    response
                )
                Log.d("TAG", "isSuccessful: ${response.data}")
                close()
            }

            override suspend fun isFail(response: Product.Response?) {
                Log.d("TAG", "isFail: ${response?.data}")
                close()
            }

            override suspend fun isNetworkError() {
                Log.d("TAG", "isNetworkError")
                close()
            }

            override suspend fun isError(errorMsg: String) {
                Log.d("TAG", "isError")
                close()
            }

        })
        awaitClose()
    }
}