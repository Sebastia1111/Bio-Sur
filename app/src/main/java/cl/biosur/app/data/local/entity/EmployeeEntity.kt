package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Empleado de BIOSUR disponible para participar en obras.
 * Lo crea la empresa. El supervisor lo selecciona al llenar un informe.
 *
 * Tabla: employees
 */
@Entity(
    tableName = "employees",
    indices = [Index(value = ["rut"], unique = true)]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val rut: String,                // formato: 12.345.678-9
    val role: String,               // cargo: Operador, Técnico, Ayudante, etc.
    val phone: String? = null,
    val email: String? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
