package com.example.commitech.data.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * Custom deserializer untuk handle integer 0/1 sebagai boolean
 */
class BooleanFromIntDeserializer : JsonDeserializer<Boolean?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Boolean? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> {
                val primitive = json.asJsonPrimitive
                when {
                    primitive.isBoolean -> primitive.asBoolean
                    primitive.isNumber -> primitive.asInt != 0
                    primitive.isString -> {
                        val str = primitive.asString.lowercase()
                        str == "true" || str == "1" || str == "ya" || str == "yes"
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
}

data class PendaftarResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("nama")
    val nama: String?, // Fix: Bisa null untuk handle error import
    @SerializedName("nim")
    val nim: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("telepon")
    val telepon: String?,
    @SerializedName("jurusan")
    val jurusan: String?,
    @SerializedName("angkatan")
    val angkatan: String?,
    @SerializedName("pilihan_divisi_1")
    val pilihanDivisi1: String?,
    @SerializedName("pilihan_divisi_2")
    val pilihanDivisi2: String?,
    @SerializedName("pilihan_divisi_3")
    val pilihanDivisi3: String?,
    @SerializedName("alasan_1")
    val alasan1: String?,
    @SerializedName("alasan_2")
    val alasan2: String?,
    @SerializedName("alasan_3")
    val alasan3: String?,
    @SerializedName("krs_terakhir")
    val krsTerakhir: String?,
    @SerializedName("formulir_pendaftaran")
    @JsonAdapter(BooleanFromIntDeserializer::class)
    val formulirPendaftaran: Boolean?,
    @SerializedName("surat_komitmen")
    @JsonAdapter(BooleanFromIntDeserializer::class)
    val suratKomitmen: Boolean?,
    @SerializedName("pindah_divisi")
    @JsonAdapter(BooleanFromIntDeserializer::class)
    val pindahDivisi: Boolean?,
    @SerializedName("tanggal_jadwal")
    val tanggalJadwal: String?,
    @SerializedName("waktu_jadwal")
    val waktuJadwal: String?,
    @SerializedName("lokasi")
    val lokasi: String?,
    @SerializedName("status_seleksi_berkas")
    val statusSeleksiBerkas: String?
)

data class PendaftarListResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<PendaftarResponse>,
    @SerializedName("pagination")
    val pagination: PaginationInfo?
)

data class PaginationInfo(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("from")
    val from: Int?,
    @SerializedName("to")
    val to: Int?,
    @SerializedName("has_more")
    val hasMore: Boolean
)

data class PendaftarSingleResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: PendaftarResponse
)

data class PesertaLulusCountData(
    @SerializedName("count")
    val count: Int
)

data class PesertaLulusCountResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: PesertaLulusCountData
)

data class ImportExcelResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: ImportData?
)

data class ImportData(
    @SerializedName("imported")
    val imported: Int,
    @SerializedName("errors")
    val errors: List<ImportError>?
)

data class ImportError(
    @SerializedName("row")
    val row: Int,
    @SerializedName("error")
    val error: String
)


