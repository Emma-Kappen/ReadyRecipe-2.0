package com.readyrecipe.android.ui.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.readyrecipe.android.data.MockDetectionRepository
import com.readyrecipe.android.domain.DetectItemsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(
    private val detectItemsUseCase: DetectItemsUseCase = DetectItemsUseCase(MockDetectionRepository())
) : ViewModel() {

    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Idle)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onDetectClicked(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = CameraUiState.Loading
            runCatching {
                detectItemsUseCase(bitmap)
            }.onSuccess { items ->
                _uiState.value = CameraUiState.Success(items)
            }.onFailure { throwable ->
                val message = throwable.message ?: "Detection failed"
                _uiState.value = CameraUiState.Error(message)
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
    }
}
