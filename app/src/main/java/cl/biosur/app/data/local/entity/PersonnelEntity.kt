package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Personal participante en un informe (campo 5).
 * Referencia al empleado + función específica que cumplió en esa obra.
 *
 * Tabla: personnel
 */
@Entity(
    tableName = "personnel",
    foreignKeys = [
        ForeignKey(
            entity = ReportEntity::class,
            parentColumns = ["id"],
            childColumns = ["reportId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("reportId"), Index("employeeId")]
)
data class PersonnelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reportId: Long,
    val employeeId: Long?,              // FK -> employees.id (nullable: podría ser personal externo)
    val name: String,                  // denormalizado por si el empleado se borra
    val functionInWork: String         // ej: "Operador principal", "Apoyo técnico"
)
