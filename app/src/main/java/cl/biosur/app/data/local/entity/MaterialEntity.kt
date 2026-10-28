package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Materiales utilizados en un informe (campo 3).
 *
 * Tabla: materials
 */
@Entity(
    tableName = "materials",
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
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val name: String,           // ej: "Límpia quimicos", "Papel higiénico"
    val quantity: Double,
    val unit: String            // ej: "L", "un", "kg"
)
