package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class AssignPesertaRequest(
    @SerializedName("peserta_ids")
    val pesertaIds: List<Int>
)

