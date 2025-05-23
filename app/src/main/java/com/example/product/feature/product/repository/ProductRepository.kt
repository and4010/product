package com.example.product.feature.product.repository


import com.example.product.api.Product
import com.example.product.feature.product.data.DataSource
import com.example.product.feature.product.model.ProductItem
import com.example.product.feature.product.model.toProductItem
import javax.inject.Inject


interface ProductInterfaces {
    suspend fun loadData() :  List<ProductItem>
    fun filterData(query: String): List<ProductItem>
    fun getProduct(id: Int) : ProductItem?
}




class ProductRepository @Inject constructor(
    val provideDataSource: DataSource
) : ProductInterfaces {


    private val data : MutableList<ProductItem> = mutableListOf()




    override suspend fun loadData() : List<ProductItem> {
        provideDataSource.callApi().collect { result ->
           result.data.map { product ->
               data.add(product.toProductItem())
           }
        }
        return data
    }

    override fun filterData(query: String): List<ProductItem> {
       return data.filter { it.martName.contains(query, ignoreCase = true) }
    }

    override fun getProduct(id: Int): ProductItem? {
        return data.find { x -> x.martId == id }
    }


}