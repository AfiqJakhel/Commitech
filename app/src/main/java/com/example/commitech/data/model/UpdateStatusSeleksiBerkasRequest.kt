package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class UpdateStatusSeleksiBerkasRequest(
    @SerializedName("status")
    val status: String
)

