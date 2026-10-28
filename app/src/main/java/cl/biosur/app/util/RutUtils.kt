package cl.biosur.app.util

/**
 * Utilidades para formatear y validar el RUT chileno.
 */
object RutUtils {

    /**
     * Valida si un RUT chileno es válido según el algoritmo de módulo 11.
     * Acepta formatos con o sin puntos y con guion (ej: "12.345.678-5", "12345678-5", "123456785").
     */
    fun isValidRut(rut: String?): Boolean {
        if (rut.isNullOrBlank()) return false
        val clean = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (clean.length < 2) return false

        val cuerpo = clean.dropLast(1)
        val dv = clean.last()

        val cuerpoInt = cuerpo.toIntOrNull() ?: return false

        return calculateDv(cuerpoInt) == dv
    }

    /**
     * Calcula el dígito verificador para un cuerpo de RUT numérico usando Módulo 11 estándar chileno.
     */
    fun calculateDv(cuerpo: Int): Char {
        var sum = 0
        var multiplier = 2
        var temp = cuerpo
        while (temp > 0) {
            sum += (temp % 10) * multiplier
            multiplier = if (multiplier == 7) 2 else multiplier + 1
            temp /= 10
        }
        val remainder = 11 - (sum % 11)
        return when (remainder) {
            11 -> '0'
            10 -> 'K'
            else -> ('0'.code + remainder).toChar()
        }
    }

    /**
     * Formatea un RUT limpio o sucio al formato estándar 12.345.678-K.
     */
    fun formatRut(rut: String?): String {
        if (rut.isNullOrBlank()) return ""
        val clean = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (clean.length < 2) return rut

        val cuerpo = clean.dropLast(1)
        val dv = clean.last()

        val sb = StringBuilder()
        var count = 0
        for (i in cuerpo.length - 1 downTo 0) {
            sb.append(cuerpo[i])
            count++
            if (count % 3 == 0 && i > 0) {
                sb.append('.')
            }
        }
        return "${sb.reverse()}-$dv"
    }
}
