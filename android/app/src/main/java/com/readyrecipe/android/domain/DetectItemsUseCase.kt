package com.readyrecipe.android.domain

import android.graphics.Bitmap
import com.readyrecipe.android.data.DetectionRepository
import com.readyrecipe.android.models.DetectedItem

class DetectItemsUseCase(
    private val detectionRepository: DetectionRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): List<DetectedItem> {
        return detectionRepository.detectItems(bitmap)
    }
}
