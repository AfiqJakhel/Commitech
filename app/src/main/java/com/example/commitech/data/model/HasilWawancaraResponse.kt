package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

data class HasilWawancaraResponse(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("peserta_id")
    val pesertaId: Int,
    
    @SerializedName("nama_peserta")
    val namaPeserta: String,
    
    @SerializedName("tanggal_jadwal")
    val tanggalJadwal: String?,
    
    @SerializedName("waktu_jadwal")
    val waktuJadwal: String?,
    
    @SerializedName("lokasi")
    val lokasi: String?,
    
    @SerializedName("status")
    val status: String,
    
    @SerializedName("divisi")
    val divisi: String?,
    
    @SerializedName("alasan")
    val alasan: String?,
    
    @SerializedName("waktu_wawancara")
    val waktuWawancara: String?,
    
    @SerializedName("dibuat_pada")
    val dibuatPada: String?,
    
    @SerializedName("diubah_pada")
    val diubahPada: String?
)

data class HasilWawancaraListResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    
    @SerializedName("pesan")
    val pesan: String?,
    
    @SerializedName("data")
    val data: List<HasilWawancaraResponse>
)

