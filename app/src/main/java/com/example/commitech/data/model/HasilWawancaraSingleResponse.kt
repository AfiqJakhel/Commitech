package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class HasilWawancaraSingleResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    
    @SerializedName("pesan")
    val pesan: String,
    
    @SerializedName("data")
    val data: HasilWawancaraResponse?
)

