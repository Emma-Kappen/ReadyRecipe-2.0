package com.readyrecipe.android.network

import com.readyrecipe.android.models.DetectedItem

data class DetectionResponse(
    val items: List<DetectedItem>
)
