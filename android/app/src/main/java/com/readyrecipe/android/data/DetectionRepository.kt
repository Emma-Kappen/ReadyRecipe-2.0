package com.readyrecipe.android.data

import android.graphics.Bitmap
import com.readyrecipe.android.models.DetectedItem

interface DetectionRepository {
    suspend fun detectItems(bitmap: Bitmap): List<DetectedItem>
}
