package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Equipos utilizados en un informe (campo 4).
 *
 * Tabla: equipment
 */
@Entity(
    tableName = "equipment",
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
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val name: String,           // ej: "Bomba de aspiración", "Manguera presión"
    val quantity: Int,
    val notes: String? = null
)
