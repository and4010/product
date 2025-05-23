package com.example.product.feature.product.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.product.feature.product.repository.ProductRepository
import com.example.product.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {



    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.onStart {
        default()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        _uiState.value
    )



    fun default(){
        val bundle = savedStateHandle.toRoute<Screen.Detail>()
        val product = productRepository.getProduct(bundle.martId)
        if(product == null) return
        _uiState.update {
            u -> u.copy(
                martId = product.martId,
                finalPrice = product.finalPrice,
                martName = product.martName,
                imageUrl = product.imageUrl
            )
        }
    }

    sealed class DetailIntent{
        data object OnBack : DetailIntent()
    }


    data class DetailState(
        val martId : Int = 0,
        val finalPrice : Int = 0,
        val martName : String = "",
        val imageUrl : String = ""
    )
}