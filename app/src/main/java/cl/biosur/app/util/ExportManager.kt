package cl.biosur.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.data.local.relation.ReportWithDetails
import com.google.gson.GsonBuilder
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exportador de informes a PDF y JSON para enviar por WhatsApp/email.
 *
 *
 * PDF con iText 5.5 (compatible Android).
 */
object ExportManager {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))
    private val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale("es", "CL"))

    /** Exporta el informe a PDF y devuelve el archivo. */
    fun exportToPdf(context: Context, report: ReportWithDetails): File {
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "informe_${report.report.id}_${fileDateFormat.format(Date())}.pdf")

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()

        // Encabezado
        val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD, BaseColor(0x0D, 0x2A, 0x4E))
        val subtitleFont = Font(Font.FontFamily.HELVETICA, 12f, Font.NORMAL, BaseColor(0x47, 0x54, 0x67))
        val sectionFont = Font(Font.FontFamily.HELVETICA, 13f, Font.BOLD, BaseColor(0x13, 0x4E, 0x8E))
        val bodyFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL, BaseColor(0x10, 0x18, 0x28))

        document.add(Paragraph("BIOSUR — Baños Químicos Sanitarios", titleFont))
        document.add(Paragraph("Informe de trabajo en terreno", subtitleFont))
        document.add(Paragraph(" "))
        document.add(Paragraph("Información general", sectionFont))

        val infoTable = PdfPTable(2).apply { widthPercentage = 100f }
        infoTable.addCell(cell("Fecha", bodyFont, true))
        infoTable.addCell(cell(dateFormat.format(Date(report.report.date)), bodyFont))
        infoTable.addCell(cell("Hora de inicio", bodyFont, true))
        infoTable.addCell(cell(dateFormat.format(Date(report.report.startTime)), bodyFont))
        infoTable.addCell(cell("Hora de término", bodyFont, true))
        infoTable.addCell(cell(dateFormat.format(Date(report.report.endTime)), bodyFont))
        infoTable.addCell(cell("Ubicación", bodyFont, true))
        infoTable.addCell(cell(formatLocation(report), bodyFont))
        infoTable.addCell(cell("Estado", bodyFont, true))
        infoTable.addCell(cell(if (report.report.synced) "Exportado" else "Pendiente", bodyFont))
        document.add(infoTable)
        document.add(Paragraph(" "))

        // Materiales
        document.add(Paragraph("Materiales utilizados", sectionFont))
        document.add(table3(report.materials.map { Triple(it.name, "${it.quantity}", it.unit) },
            arrayOf("Material", "Cantidad", "Unidad"), bodyFont))
        document.add(Paragraph(" "))

        // Equipos
        document.add(Paragraph("Equipos utilizados", sectionFont))
        document.add(table3(report.equipment.map { Triple(it.name, "${it.quantity}", it.notes ?: "—") },
            arrayOf("Equipo", "Cantidad", "Notas"), bodyFont))
        document.add(Paragraph(" "))

        // Personal
        document.add(Paragraph("Personal participante", sectionFont))
        document.add(table3(report.personnel.map { Triple(it.name, it.functionInWork, "—") },
            arrayOf("Nombre", "Función", "Observación"), bodyFont))
        document.add(Paragraph(" "))

        // Evidencias
        document.add(Paragraph("Evidencias multimedia", sectionFont))
        if (report.media.isEmpty()) {
            document.add(Paragraph("Sin evidencias registradas.", bodyFont))
        } else {
            report.media.forEachIndexed { idx, m ->
                document.add(Paragraph("${idx + 1}. ${m.type} — ${m.comment ?: "Sin comentario"}", bodyFont))
            }
        }
        document.add(Paragraph(" "))

        // Comentarios
        document.add(Paragraph("Comentarios del trabajo", sectionFont))
        document.add(Paragraph(report.report.comments.ifBlank { "Sin comentarios." }, bodyFont))

        document.close()
        return file
    }

    /** Exporta el informe a JSON y devuelve el archivo. */
    fun exportToJson(context: Context, report: ReportWithDetails): File {
        val reportsDir = File(context.filesDir, "reports").apply { mkdirs() }
        val file = File(reportsDir, "informe_${report.report.id}_${fileDateFormat.format(Date())}.json")
        val gson = GsonBuilder().setPrettyPrinting().create()
        file.writeText(gson.toJson(report), Charsets.UTF_8)
        return file
    }

    /** Lanza el Intent de compartir (WhatsApp, email, etc.). */
    fun share(context: Context, file: File, title: String = "Compartir informe BIOSUR") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "json" -> "application/json"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    // ===== Helpers =====

    private fun cell(text: String, font: Font, header: Boolean = false): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        if (header) {
            cell.backgroundColor = BaseColor(0xE8, 0xF1, 0xFB)
            cell.borderColor = BaseColor(0x13, 0x4E, 0x8E)
        }
        cell.paddingLeft = 8f
        cell.paddingTop = 6f
        cell.paddingBottom = 6f
        return cell
    }

    private fun table3(rows: List<Triple<String, String, String>>,
                      headers: Array<String>, font: Font): PdfPTable {
        val t = PdfPTable(3).apply { widthPercentage = 100f }
        headers.forEach { t.addCell(cell(it, font, header = true)) }
        rows.forEach { (a, b, c) ->
            t.addCell(cell(a, font))
            t.addCell(cell(b, font))
            t.addCell(cell(c, font))
        }
        return t
    }

    private fun formatLocation(report: ReportWithDetails): String {
        val r = report.report
        return when {
            r.latitude != null && r.longitude != null ->
                "%.5f, %.5f — %s".format(r.latitude, r.longitude, r.address ?: "Dirección no disponible")
            else -> "Ubicación no registrada"
        }
    }
}
