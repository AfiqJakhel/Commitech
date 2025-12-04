package com.example.commitech.data.model

import com.google.gson.annotations.SerializedName

/**
 * Wrapper Response untuk single hasil wawancara dari backend API
 * 
 * Fitur: Modul 4 - Fitur 16: Input Hasil Wawancara (Create)
 * Endpoint: POST /api/wawancara/hasil
 * 
 * Backend mengembalikan response dalam format:
 * ```json
 * {
 *   "sukses": true,
 *   "pesan": "Hasil wawancara berhasil disimpan",
 *   "data": { ... }
 * }
 * ```
 * 
 * Model ini digunakan untuk wrap response tersebut sesuai dengan format backend Laravel.
 * 
 * @property sukses Boolean yang menandakan apakah request berhasil
 * @property pesan Pesan dari server (bisa berupa pesan sukses atau error)
 * @property data Data hasil wawancara (null jika error)
 */
data class HasilWawancaraSingleResponse(
    @SerializedName("sukses")
    val sukses: Boolean,
    
    @SerializedName("pesan")
    val pesan: String,
    
    @SerializedName("data")
    val data: HasilWawancaraResponse?
)

