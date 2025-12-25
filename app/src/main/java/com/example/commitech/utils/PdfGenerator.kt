package com.example.commitech.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.commitech.ui.viewmodel.DivisiData
import com.example.commitech.ui.viewmodel.ParticipantInfo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {
    
    private const val PAGE_WIDTH = 595 // A4 width in points (210mm)
    private const val PAGE_HEIGHT = 842 // A4 height in points (297mm)
    private const val MARGIN = 50
    private const val LINE_HEIGHT = 20
    private const val TABLE_HEADER_HEIGHT = 30
    private const val ROW_HEIGHT = 25

    fun generatePengumumanPDF(
        context: Context,
        daftarDivisi: List<DivisiData>,
        onSuccess: (android.net.Uri) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Filter hanya divisi yang punya peserta
            val divisiDenganPeserta = daftarDivisi.filter { it.pesertaLulus.isNotEmpty() }
            
            if (divisiDenganPeserta.isEmpty()) {
                onError("Tidak ada peserta yang lulus untuk diekspor")
                return
            }
            
            // Create PDF document
            val document = PdfDocument()
            
            // Create page
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas
            
            var yPosition = MARGIN.toFloat()
            
            // Title
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("PENGUMUMAN KELULUSAN SELEKSI", PAGE_WIDTH / 2f, yPosition, titlePaint)
            yPosition += 40
            
            // Subtitle
            val subtitlePaint = Paint().apply {
                color = Color.GRAY
                textSize = 14f
                textAlign = Paint.Align.CENTER
            }
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            canvas.drawText("Panitia BEM KM FTI", PAGE_WIDTH / 2f, yPosition, subtitlePaint)
            yPosition += 20
            canvas.drawText("Tanggal: ${dateFormat.format(Date())}", PAGE_WIDTH / 2f, yPosition, subtitlePaint)
            yPosition += 40
            
            // Table for each division
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
            }
            
            val headerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 14f
                isFakeBoldText = true
            }
            
            val headerBgPaint = Paint().apply {
                color = Color.parseColor("#9C27B0") // Purple
            }
            
            val cellBorderPaint = Paint().apply {
                color = Color.GRAY
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            
            divisiDenganPeserta.forEachIndexed { index, divisi ->
                // Check if we need a new page
                val estimatedHeight = TABLE_HEADER_HEIGHT + (divisi.pesertaLulus.size * ROW_HEIGHT) + 40
                if (yPosition + estimatedHeight > PAGE_HEIGHT - MARGIN) {
                    document.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, document.pages.size + 1).create()
                    page = document.startPage(newPageInfo)
                    canvas = page.canvas
                    yPosition = MARGIN.toFloat()
                }
                
                // Division header
                val divisionHeaderPaint = Paint().apply {
                    color = Color.parseColor("#9C27B0")
                    textSize = 16f
                    isFakeBoldText = true
                }
                yPosition += 20
                canvas.drawText(
                    "Divisi ${divisi.namaDivisi}",
                    MARGIN.toFloat(),
                    yPosition,
                    divisionHeaderPaint
                )
                yPosition += 5
                canvas.drawText(
                    "Koordinator: ${divisi.koordinator}",
                    MARGIN.toFloat(),
                    yPosition,
                    textPaint.apply { textSize = 11f; color = Color.GRAY }
                )
                yPosition += 25
                
                // Table header
                val tableStartX = MARGIN.toFloat()
                val tableEndX = PAGE_WIDTH - MARGIN.toFloat()
                val colNoWidth = 40f
                val colNamaWidth = tableEndX - tableStartX - colNoWidth
                
                // Header background
                canvas.drawRect(
                    tableStartX,
                    yPosition - TABLE_HEADER_HEIGHT,
                    tableEndX,
                    yPosition,
                    headerBgPaint
                )
                
                // Header text
                canvas.drawText("No", tableStartX + 10, yPosition - 10, headerPaint)
                canvas.drawText("Nama Peserta", tableStartX + colNoWidth + 10, yPosition - 10, headerPaint)
                
                // Header border
                canvas.drawRect(
                    tableStartX,
                    yPosition - TABLE_HEADER_HEIGHT,
                    tableEndX,
                    yPosition,
                    cellBorderPaint
                )
                canvas.drawLine(
                    tableStartX + colNoWidth,
                    yPosition - TABLE_HEADER_HEIGHT,
                    tableStartX + colNoWidth,
                    yPosition,
                    cellBorderPaint
                )
                
                yPosition += 5
                
                // Table rows
                divisi.pesertaLulus.forEachIndexed { pesertaIndex, peserta ->
                    val rowY = yPosition
                    
                    // Row background (alternating)
                    if (pesertaIndex % 2 == 0) {
                        val rowBgPaint = Paint().apply {
                            color = Color.parseColor("#F5F5F5")
                        }
                        canvas.drawRect(
                            tableStartX,
                            rowY - ROW_HEIGHT,
                            tableEndX,
                            rowY,
                            rowBgPaint
                        )
                    }
                    
                    // Row content
                    canvas.drawText(
                        "${pesertaIndex + 1}",
                        tableStartX + 10,
                        rowY - 8,
                        textPaint
                    )
                    
                    // Wrap text if too long
                    val namaText = peserta.name
                    val maxWidth = colNamaWidth - 20
                    val namaPaint = Paint().apply {
                        color = Color.BLACK
                        textSize = 11f
                    }
                    
                    canvas.drawText(
                        namaText,
                        tableStartX + colNoWidth + 10,
                        rowY - 8,
                        namaPaint
                    )
                    
                    // Row border
                    canvas.drawRect(
                        tableStartX,
                        rowY - ROW_HEIGHT,
                        tableEndX,
                        rowY,
                        cellBorderPaint
                    )
                    canvas.drawLine(
                        tableStartX + colNoWidth,
                        rowY - ROW_HEIGHT,
                        tableStartX + colNoWidth,
                        rowY,
                        cellBorderPaint
                    )
                    
                    yPosition += ROW_HEIGHT
                }
                
                yPosition += 20
            }
            
            // Footer
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            yPosition = PAGE_HEIGHT - MARGIN.toFloat()
            canvas.drawText(
                "Dokumen ini dihasilkan secara otomatis oleh sistem BEM KM FTI",
                PAGE_WIDTH / 2f,
                yPosition,
                footerPaint
            )
            
            document.finishPage(page)
            
            // Save PDF to file
            val fileName = "Pengumuman_Kelulusan_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            file.parentFile?.exists()?.let {
                if (!it) {
                    file.parentFile?.mkdirs()
                }
            }
            
            val fileOutputStream = FileOutputStream(file)
            document.writeTo(fileOutputStream)
            document.close()
            fileOutputStream.close()
            
            // Get URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            onSuccess(uri)
            
        } catch (e: Exception) {
            onError("Gagal membuat PDF: ${e.message}")
        }
    }
}

