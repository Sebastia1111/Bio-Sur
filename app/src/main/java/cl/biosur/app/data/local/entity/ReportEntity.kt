package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Informe de trabajo en terreno.
 *
 * Cubre:
 *  - Campo 1: fecha + hora inicio + hora término
 *  - Campo 6: ubicación GPS (lat, lon, dirección)
 *  - Estado de sincronización offline (campo 7)
 *
 * Tabla: reports
 */
@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supervisorId: Long,              // FK -> users.id
    val date: Long,                      // epoch millis — fecha del trabajo
    val startTime: Long,                 // epoch millis — hora inicio
    val endTime: Long,                   // epoch millis — hora término
    val comments: String = "",           // campo 2 (comentarios generales)
    val latitude: Double? = null,        // campo 6
    val longitude: Double? = null,
    val address: String? = null,
    val status: String = "DRAFT",         // ReportStatus.value
    val synced: Boolean = false,         // campo 7 — false = pendiente sincronizar
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
