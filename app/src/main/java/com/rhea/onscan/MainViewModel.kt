package com.rhea.onscan

import android.content.ClipData
import android.content.ClipboardManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val clipboardManager: ClipboardManager
): ViewModel() {

    val qrType = savedStateHandle.getLiveData<QrType>(QR_TYPE_KEY)

    private val _scannedAddress = MutableLiveData<String>()
    val scannedAddress = _scannedAddress as LiveData<String>

    private val _scannedResult = MutableLiveData<Boolean>()
    val scannedResult = _scannedResult as LiveData<Boolean>


    fun setQrType(type: QrType){
        savedStateHandle[QR_TYPE_KEY] = type
    }

    fun setScannedAddress(address: String) {
        _scannedAddress.postValue(address)
        validateAddress(address)
    }

    private fun validateAddress(address: String) {
        val result = if (qrType.value == QrType.ETH)
            validateETHAddress(address)
        else validateBTCAddress(address)
        _scannedResult.postValue(result)
    }

    private fun validateBTCAddress(address: String): Boolean {
        val pattern = Pattern.compile("^(1)[a-hj-km-zA-NP-Z1-9]{24,33}")
        return pattern.matcher(address).matches()
    }

    private fun validateETHAddress(address: String): Boolean {
        val pattern = Pattern.compile("^(0x)[0-9a-f]*$")
        return pattern.matcher(address).matches()
    }

    fun copyResult() {
        scannedAddress.value?.let {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("copyLabel", it))
        }
    }

    companion object {
        const val QR_TYPE_KEY = "qr_type_key"
        enum class QrType {
            ETH, BTC
        }
    }
}