package cl.biosur.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import cl.biosur.app.data.model.UserRole
import java.security.MessageDigest

/**
 * Sesión del usuario actual (SharedPreferences encriptadas).
 *
 * Nota: si EncryptedSharedPreferences falla en algún dispositivo, hay un fallback
 * a SharedPreferences plano (no es ideal, pero evita crasheos en dev).
 */
object SessionManager {

    private const val PREFS_NAME = "biosur_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_FULL_NAME = "full_name"

    private fun prefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            // Fallback en desarrollo
            context.getSharedPreferences("${PREFS_NAME}_plain", Context.MODE_PRIVATE)
        }
    }

    fun saveSession(userId: Long, email: String, role: String, fullName: String) {
        val ctx = BiosurAppContextHolder.appContext
            ?: error("BiosurAppContextHolder no inicializado")
        prefs(ctx).edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .putString(KEY_FULL_NAME, fullName)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        val ctx = BiosurAppContextHolder.appContext ?: return false
        return prefs(ctx).contains(KEY_USER_ID)
    }

    fun isCompany(): Boolean = currentRole() == UserRole.COMPANY.value

    fun isSupervisor(): Boolean = currentRole() == UserRole.SUPERVISOR.value

    fun currentUserId(): Long? {
        val ctx = BiosurAppContextHolder.appContext ?: return null
        val id = prefs(ctx).getLong(KEY_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    fun currentRole(): String? =
        BiosurAppContextHolder.appContext?.let { prefs(it).getString(KEY_ROLE, null) }

    fun currentEmail(): String? =
        BiosurAppContextHolder.appContext?.let { prefs(it).getString(KEY_EMAIL, null) }

    fun currentFullName(): String? =
        BiosurAppContextHolder.appContext?.let { prefs(it).getString(KEY_FULL_NAME, null) }

    fun clear() {
        val ctx = BiosurAppContextHolder.appContext ?: return
        prefs(ctx).edit().clear().apply()
    }

    /**
     * Hash de contraseña (SHA-256 + salt por email).
     * Suficiente para una app local sin backend; usar Argon2/PBKDF2 si se quiere más robusto.
     */
    fun hashPassword(password: String, email: String): String {
        val salt = "biosur::$email"
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest((salt + password).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
