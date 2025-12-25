package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class HasilWawancaraRequest(
    @SerializedName("peserta_id")
    val pesertaId: Int,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("divisi")
    val divisi: String?,
    
    @SerializedName("alasan")
    val alasan: String?
)

