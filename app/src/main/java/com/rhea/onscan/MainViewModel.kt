package com.rhea.onscan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    val qrType = savedStateHandle.getLiveData<QrType>(QR_TYPE_KEY)

    fun setQrType(type: QrType){
        savedStateHandle[QR_TYPE_KEY] = type
    }

    companion object {
        const val QR_TYPE_KEY = "qr_type_key"
        enum class QrType {
            ETH, BTC
        }
    }

}