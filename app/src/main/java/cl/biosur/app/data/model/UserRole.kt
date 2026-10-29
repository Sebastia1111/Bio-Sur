package cl.biosur.app.data.model

/**
 * Roles de usuario en BIOSUR.
 *  - COMPANY: cuenta de empresa, crea cuentas de supervisor y gestiona empleados.
 *  - SUPERVISOR: sube informes con evidencias desde terreno.
 */
enum class UserRole(val value: String) {
    COMPANY("COMPANY"),
    SUPERVISOR("SUPERVISOR");

    companion object {
        fun from(value: String?): UserRole = entries.firstOrNull { it.value == value } ?: SUPERVISOR
    }
}
