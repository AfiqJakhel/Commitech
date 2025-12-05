package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class AssignPesertaRequest(
    @SerializedName("peserta_ids")
    val pesertaIds: List<Int>
)

data class AssignPesertaResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    @SerializedName("pesan")
    val pesan: String,
    @SerializedName("data")
    val data: AssignPesertaData?
)

data class AssignPesertaData(
    @SerializedName("jadwal_id")
    val jadwalId: Int,
    @SerializedName("peserta_count")
    val pesertaCount: Int,
    @SerializedName("peserta_ids")
    val pesertaIds: List<Int>
)

