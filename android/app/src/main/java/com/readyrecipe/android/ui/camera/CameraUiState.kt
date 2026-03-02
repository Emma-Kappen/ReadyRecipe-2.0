package com.readyrecipe.android.ui.camera

import com.readyrecipe.android.models.DetectedItem

sealed interface CameraUiState {
    data object Idle : CameraUiState
    data object Loading : CameraUiState
    data class Success(val items: List<DetectedItem>) : CameraUiState
    data class Error(val message: String) : CameraUiState
}
