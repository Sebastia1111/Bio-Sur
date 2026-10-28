package cl.biosur.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Usuario del sistema. Sólo la empresa (rol COMPANY) puede crear cuentas de supervisor.
 * La contraseña se almacena con hash simple (PBKDF2) — ver [cl.biosur.app.util.SessionManager].
 *
 * Tabla: users
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val fullName: String,
    val role: String,           // UserRole.value
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
