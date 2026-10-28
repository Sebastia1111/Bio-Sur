package cl.biosur.app.util

import android.app.Application

/**
 * Holders para acceder al contexto de la aplicación desde utilitarios que no son
 * Activities/Fragments (SessionManager, ExportManager, etc.).
 *
 * Debe inicializarse en BiosurApp.onCreate().
 */
object BiosurAppContextHolder {
    @Volatile
    var appContext: android.content.Context? = null
        private set

    fun init(app: Application) {
        appContext = app.applicationContext
    }
}
