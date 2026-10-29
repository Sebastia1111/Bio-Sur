package cl.biosur.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import cl.biosur.app.ServiceLocator
import cl.biosur.app.data.local.relation.ReportWithDetails
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Módulo para respaldo, exportación e importación de informes en formato JSON.
 * Permite la sincronización e intercambio local de informes entre dos o más teléfonos.
 */
object BackupSyncManager {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Exporta todos los informes locales con sus detalles a un archivo JSON.
     */
    suspend fun createBackupFile(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            val repo = ServiceLocator.reportRepository
            val reportsList = repo.observeAllWithDetails().first()

            val jsonString = gson.toJson(reportsList)
            val file = File(context.cacheDir, "biosur_backup_reports.json")
            file.writeText(jsonString, Charsets.UTF_8)

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea un Intent para compartir el archivo de respaldo JSON mediante Bluetooth, Email, WhatsApp, etc.
     */
    fun createShareIntent(context: Context, fileUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Respaldo de Informes BIOSUR")
            putExtra(Intent.EXTRA_TEXT, "Adjunto respaldo en JSON de los informes de campo de BIOSUR.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Importa informes desde una cadena JSON e inserta o actualiza en la base de datos local Room.
     * Retorna la cantidad de informes procesados e importados.
     */
    suspend fun importBackupFromJson(jsonString: String): Int = withContext(Dispatchers.IO) {
        try {
            val type = object : TypeToken<List<ReportWithDetails>>() {}.type
            val importedList: List<ReportWithDetails> = gson.fromJson(jsonString, type) ?: emptyList()

            val repo = ServiceLocator.reportRepository
            var count = 0

            for (item in importedList) {
                repo.saveFullReport(
                    report = item.report,
                    materials = item.materials,
                    equipment = item.equipment,
                    personnel = item.personnel,
                    media = item.media
                )
                count++
            }
            count
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
}
