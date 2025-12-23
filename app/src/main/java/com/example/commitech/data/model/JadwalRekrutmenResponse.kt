package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class JadwalRekrutmenItem(
    @SerializedName("id")
    val id: Int,
    @SerializedName("judul")
    val judul: String,
    @SerializedName("tanggal_mulai")
    val tanggalMulai: String,
    @SerializedName("tanggal_selesai")
    val tanggalSelesai: String,
    @SerializedName("waktu_mulai")
    val waktuMulai: String,
    @SerializedName("waktu_selesai")
    val waktuSelesai: String,
    @SerializedName("pewawancara")
    val pewawancara: String?,
    @SerializedName("lokasi")
    val lokasi: String?
)

data class JadwalRekrutmenResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    @SerializedName("pesan")
    val pesan: String,
    @SerializedName("data")
    val data: List<JadwalRekrutmenItem>
)

data class JadwalRekrutmenSingleResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    @SerializedName("pesan")
    val pesan: String,
    @SerializedName("data")
    val data: JadwalRekrutmenItem
)

