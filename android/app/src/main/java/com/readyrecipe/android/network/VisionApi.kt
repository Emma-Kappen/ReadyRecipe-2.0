package com.readyrecipe.android.network

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface VisionApi {
    @Multipart
    @POST("/api/vision/detect")
    suspend fun detectItems(
        @Part image: MultipartBody.Part
    ): Response<DetectionResponse>
}
