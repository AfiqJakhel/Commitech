package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Response Data hasil wawancara dari backend API
 * 
 * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Create)
 * Endpoint: POST /api/wawancara/hasil
 * 
 * Model ini merepresentasikan data hasil wawancara yang dikembalikan oleh server
 * setelah berhasil menyimpan hasil wawancara.
 * 
 * @property id ID hasil wawancara di database
 * @property pesertaId ID peserta yang diwawancara
 * @property namaPeserta Nama peserta (diambil dari relasi Peserta)
 * @property tanggalJadwal Tanggal jadwal wawancara
 * @property waktuJadwal Waktu jadwal wawancara
 * @property lokasi Lokasi wawancara
 * @property status Status hasil: "pending", "diterima", atau "ditolak"
 * @property divisi Nama divisi (jika diterima)
 * @property alasan Alasan penolakan (jika ditolak)
 * @property waktuWawancara Waktu kapan hasil wawancara disimpan (ISO 8601 format)
 * @property dibuatPada Timestamp kapan data dibuat (ISO 8601 format)
 * @property diubahPada Timestamp kapan data terakhir diubah (ISO 8601 format)
 */
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
    val status: String,  // "pending", "diterima", atau "ditolak"
    
    @SerializedName("divisi")
    val divisi: String?,
    
    @SerializedName("alasan")
    val alasan: String?,
    
    @SerializedName("waktu_wawancara")
    val waktuWawancara: String?,  // ISO 8601 format
    
    @SerializedName("dibuat_pada")
    val dibuatPada: String?,  // ISO 8601 format
    
    @SerializedName("diubah_pada")
    val diubahPada: String?   // ISO 8601 format
)

/**
 * Model untuk Response List hasil wawancara dari backend API
 * 
 * Endpoint: GET /api/wawancara/hasil
 */
data class HasilWawancaraListResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    
    @SerializedName("pesan")
    val pesan: String?,
    
    @SerializedName("data")
    val data: List<HasilWawancaraResponse>
)

