package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("errors")
    val errors: Map<String, List<String>>? = null
)

