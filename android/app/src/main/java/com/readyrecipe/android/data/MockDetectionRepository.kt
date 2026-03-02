package com.readyrecipe.android.data

import android.graphics.Bitmap
import com.readyrecipe.android.models.DetectedItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MockDetectionRepository : DetectionRepository {
    override suspend fun detectItems(bitmap: Bitmap): List<DetectedItem> = withContext(Dispatchers.IO) {
        delay(1000)
        listOf(
            DetectedItem(name = "tomato", confidence = 0.93f),
            DetectedItem(name = "milk", confidence = 0.88f),
            DetectedItem(name = "onion", confidence = 0.91f)
        )
    }
}
