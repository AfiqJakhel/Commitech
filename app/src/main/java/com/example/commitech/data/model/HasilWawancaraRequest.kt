package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model untuk Request Body saat menyimpan hasil wawancara ke backend API
 * 
 * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Create)
 * Endpoint: POST /api/wawancara/hasil
 * 
 * @property pesertaId ID peserta dari database (REQUIRED)
 * @property status Status hasil wawancara: "pending", "diterima", atau "ditolak" (REQUIRED)
 * @property divisi Nama divisi (REQUIRED jika status = "diterima", nullable jika tidak)
 * @property alasan Alasan penolakan (OPTIONAL, biasanya untuk status "ditolak")
 * 
 * Contoh penggunaan:
 * ```
 * val request = HasilWawancaraRequest(
 *     pesertaId = 1,
 *     status = "diterima",
 *     divisi = "Acara",
 *     alasan = null
 * )
 * ```
 */
data class HasilWawancaraRequest(
    @SerializedName("peserta_id")
    val pesertaId: Int,
    
    @SerializedName("status")
    val status: String,  // "pending", "diterima", atau "ditolak"
    
    @SerializedName("divisi")
    val divisi: String?,  // Required jika status = "diterima"
    
    @SerializedName("alasan")
    val alasan: String?   // Optional, untuk status "ditolak"
)

