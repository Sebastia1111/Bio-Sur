package cl.biosur.app.data.repository

import cl.biosur.app.data.local.dao.UserDao
import cl.biosur.app.data.local.entity.UserEntity
import cl.biosur.app.data.model.UserRole
import cl.biosur.app.util.SessionManager
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de autenticación y gestión de usuarios.
 *
 * NOTA PARA EL COMPAÑERO 1:
 *  - Aquí va la lógica de hash/verificación de contraseña.
 *  - El seed inicial (cuenta empresa por defecto) se crea en [BiosurApp.onCreate]
 *    usando [ensureDefaultCompanyAccount].
 *  - El hash actual es SHA-256 con salt fijo (suficiente para una app local sin backend).
 *    Si quieren más seguridad, migren a PBKDF2 en [SessionManager].
 */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    suspend fun login(email: String, password: String): LoginResult {
        val user = userDao.findByEmail(email.trim().lowercase())
            ?: return LoginResult.Error("Usuario no encontrado")
        if (!user.active) return LoginResult.Error("Cuenta inactiva")
        val hash = SessionManager.hashPassword(password, user.email)
        if (!hash.contentEquals(user.passwordHash)) {
            return LoginResult.Error("Contraseña incorrecta")
        }
        sessionManager.saveSession(user.id, user.email, user.role, user.fullName)
        return LoginResult.Success(user)
    }

    suspend fun createSupervisorAccount(
        email: String,
        password: String,
        fullName: String
    ): Long {
        require(email.isNotBlank() && password.length >= 6) {
            "Email inválido o contraseña muy corta (mín 6 caracteres)"
        }
        val hash = SessionManager.hashPassword(password, email.trim().lowercase())
        val user = UserEntity(
            email = email.trim().lowercase(),
            passwordHash = hash,
            fullName = fullName,
            role = UserRole.SUPERVISOR.value
        )
        return userDao.insert(user)
    }

    suspend fun toggleActive(userId: Long, active: Boolean) {
        userDao.setActive(userId, active)
    }

    fun observeSupervisors(): Flow<List<UserEntity>> = userDao.observeSupervisors()
    fun observeSupervisorCount(): Flow<Int> = userDao.observeSupervisorCount()

    fun logout() = sessionManager.clear()

    sealed interface LoginResult {
        data class Success(val user: UserEntity) : LoginResult
        data class Error(val message: String) : LoginResult
    }
}
