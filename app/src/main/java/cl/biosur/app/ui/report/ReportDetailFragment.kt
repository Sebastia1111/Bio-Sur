package cl.biosur.app.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cl.biosur.app.ServiceLocator
import cl.biosur.app.databinding.FragmentReportDetailBinding
import cl.biosur.app.util.ExportManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Detalle de un informe con opción de exportar a PDF/JSON.
 *  3 (UI de detalle + exportación).
 *
 * La empresa y el supervisor pueden verlo; ambos pueden exportar.
 */
class ReportDetailFragment : Fragment() {

    private var _binding: FragmentReportDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels()

    private val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val reportId = arguments?.getLong("reportId") ?: -1L
        if (reportId == -1L) {
            findNavController().popBackStack()
            return
        }

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        viewLifecycleOwner.lifecycleScope.launch {
            val report = withContext(Dispatchers.IO) {
                ServiceLocator.reportRepository.getWithDetails(reportId)
            }
            if (report == null) {
                Snackbar.make(view, "Informe no encontrado", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@launch
            }
            bindReport(report)

            binding.btnExportPdf.setOnClickListener {
                val file = ExportManager.exportToPdf(requireContext(), report)
                viewModel.markExported(reportId)
                ExportManager.share(requireContext(), file, "Compartir informe PDF")
            }
            binding.btnExportJson.setOnClickListener {
                val file = ExportManager.exportToJson(requireContext(), report)
                viewModel.markExported(reportId)
                ExportManager.share(requireContext(), file, "Compartir informe JSON")
            }
        }
    }

    private fun bindReport(report: cl.biosur.app.data.local.relation.ReportWithDetails) {
        val r = report.report
        binding.toolbar.title = "Informe #${r.id}"
        binding.detailSummary.text = buildString {
            append("• INFORMACIÓN GENERAL\n")
            append("Fecha del informe: ").append(df.format(Date(r.date))).append("\n")
            append("Hora de inicio:    ").append(df.format(Date(r.startTime))).append("\n")
            append("Hora de término:   ").append(df.format(Date(r.endTime))).append("\n")
            append("Ubicación GPS:     ")
            if (r.latitude != null && r.longitude != null) {
                append("%.5f, %.5f".format(r.latitude, r.longitude))
                r.address?.let { append(" (${it})") }
            } else {
                append("No registrada")
            }
            append("\n")
            append("Estado:            ").append(if (r.synced) "Exportado / Sincronizado" else "Pendiente de exportar").append("\n\n")

            append("• MATERIALES UTILIZADOS (${report.materials.size})\n")
            if (report.materials.isEmpty()) {
                append("  (Sin materiales registrados)\n")
            } else {
                report.materials.forEach { m ->
                    append("  - ${m.name}: ${m.quantity} ${m.unit}\n")
                }
            }
            append("\n")

            append("• EQUIPOS UTILIZADOS (${report.equipment.size})\n")
            if (report.equipment.isEmpty()) {
                append("  (Sin equipos registrados)\n")
            } else {
                report.equipment.forEach { e ->
                    val notes = if (!e.notes.isNullOrBlank()) " [${e.notes}]" else ""
                    append("  - ${e.name} (Cant: ${e.quantity})$notes\n")
                }
            }
            append("\n")

            append("• PERSONAL PARTICIPANTE (${report.personnel.size})\n")
            if (report.personnel.isEmpty()) {
                append("  (Sin personal registrado)\n")
            } else {
                report.personnel.forEach { p ->
                    append("  - ${p.name} — ${p.functionInWork}\n")
                }
            }
            append("\n")

            append("• EVIDENCIAS MULTIMEDIA (${report.media.size})\n")
            if (report.media.isEmpty()) {
                append("  (Sin evidencias adjuntas)\n")
            } else {
                report.media.forEachIndexed { i, m ->
                    val comment = if (!m.comment.isNullOrBlank()) ": ${m.comment}" else ""
                    append("  ${i + 1}. [${m.type}]$comment\n")
                }
            }
            append("\n")

            append("• COMENTARIOS DEL TRABAJO\n")
            append(r.comments.ifBlank { "Sin comentarios adicionales." })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
