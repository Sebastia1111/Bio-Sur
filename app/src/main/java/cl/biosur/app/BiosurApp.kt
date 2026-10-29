package cl.biosur.app

import android.app.Application
import cl.biosur.app.data.local.BiosurDatabase
import cl.biosur.app.data.local.entity.UserEntity
import cl.biosur.app.data.model.UserRole
import cl.biosur.app.util.BiosurAppContextHolder
import cl.biosur.app.util.OfflineManager
import cl.biosur.app.util.SessionManager
import cl.biosur.app.util.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class BIOSUR.
 *  - Inicializa el contexto global.
 *  - Inicializa el monitor de conectividad (offline).
 *  - Crea las cuentas por defecto (admin@biosur.cl y supervisor@biosur.cl / biosur2024).
 *  - Expone los repositorios como singletons perezosos.
 */
class BiosurApp : Application() {

    override fun onCreate() {
        super.onCreate()
        BiosurAppContextHolder.init(this)
        ThemeManager.init(this)
        OfflineManager.init(this)
        ensureDefaultAccounts()
    }

    /**
     * Crea cuentas de prueba si no existen:
     * - Empresa:    admin@biosur.cl      | Contraseña: biosur2024
     * - Supervisor: supervisor@biosur.cl | Contraseña: biosur2024
     */
    private fun ensureDefaultAccounts() {
        appScope.launch {
            try {
                val userDao = BiosurDatabase.get(this@BiosurApp).userDao()

                // Cuenta Empresa
                val adminEmail = "admin@biosur.cl"
                if (userDao.findByEmail(adminEmail) == null) {
                    val hash = SessionManager.hashPassword("biosur2024", adminEmail)
                    userDao.insert(
                        UserEntity(
                            email = adminEmail,
                            passwordHash = hash,
                            fullName = "Empresa BIOSUR",
                            role = UserRole.COMPANY.value
                        )
                    )
                }

                // Cuenta Supervisor
                val supEmail = "supervisor@biosur.cl"
                if (userDao.findByEmail(supEmail) == null) {
                    val hash = SessionManager.hashPassword("biosur2024", supEmail)
                    userDao.insert(
                        UserEntity(
                            email = supEmail,
                            passwordHash = hash,
                            fullName = "Supervisor Terreno",
                            role = UserRole.SUPERVISOR.value
                        )
                    )
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: BiosurApp? = null

        fun get(): BiosurApp = INSTANCE ?: error("BiosurApp no inicializada")

        /** Coroutine scope a nivel aplicación para tareas en background. */
        val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    init {
        INSTANCE = this
    }
}
