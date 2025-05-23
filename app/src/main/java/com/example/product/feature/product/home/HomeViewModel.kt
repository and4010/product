package com.example.product.feature.product.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.product.feature.product.model.ProductItem
import com.example.product.feature.product.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductState())
    val uiState: StateFlow<ProductState> = _uiState.onStart {
        initData()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _uiState.value
    )



    fun initData(){
        viewModelScope.launch {
            val data = productRepository.loadData()
            _uiState.update {
                it.copy(data = data)
            }
        }

    }

    fun onIntent(intent: HomeIntent) {
        when(intent){
            is HomeIntent.NextPage -> Unit
            is HomeIntent.FilterText -> filterText(intent.input)
        }
    }

    fun filterText(input: String) {
        val data = productRepository.filterData(input)
        _uiState.update {
            it.copy(data = data, filterText = input)
        }
    }

    sealed class HomeIntent{
        data class NextPage(val id : Int) : HomeIntent()
        data class FilterText(val input: String) : HomeIntent()
    }


    data class ProductState(
        val data : List<ProductItem> = emptyList(),
        val filterText : String = ""
    )

}

