package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Evidencia multimedia de un informe (campo 2 — imágenes/videos + comentarios).
 * La ruta apunta a un archivo local en app/files/media/.
 *
 * Tabla: media
 */
@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reportId")]
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val type: String,           // "IMAGE" o "VIDEO"
    val filePath: String,       // ruta absoluta en almacenamiento interno
    val thumbnailPath: String? = null,
    val comment: String? = null,
    val capturedAt: Long = System.currentTimeMillis()
)
