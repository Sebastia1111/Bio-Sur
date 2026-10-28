package cl.biosur.app.data.model

/**
 * Estado de un informe.
 *  - DRAFT: en edición local.
 *  - READY: listo para ser entregado / exportado.
 *  - EXPORTED: ya fue exportado a PDF/JSON.
 */
enum class ReportStatus(val value: String) {
    DRAFT("DRAFT"),
    READY("READY"),
    EXPORTED("EXPORTED");

    companion object {
        fun from(value: String?): ReportStatus = entries.firstOrNull { it.value == value } ?: DRAFT
    }
}
