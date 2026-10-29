package cl.biosur.app.ui.report.steps

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cl.biosur.app.databinding.StepReportDatetimeBinding
import cl.biosur.app.ui.report.ReportViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Paso 1: Fecha + hora inicio + hora término.
 * Responsabilidad: Compañero 2.
 */
class StepDateTimeFragment : Fragment() {

    private var _binding: StepReportDatetimeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by activityViewModels()

    private val df = SimpleDateFormat("EEEE dd 'de' MMMM yyyy", Locale("es", "CL"))
    private val tf = SimpleDateFormat("HH:mm", Locale("es", "CL"))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = StepReportDatetimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateLabels()

        binding.btnPickDate.setOnClickListener { pickDate() }
        binding.btnPickStart.setOnClickListener { pickTime(true) }
        binding.btnPickEnd.setOnClickListener { pickTime(false) }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.reportDate }
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d)
            viewModel.reportDate = cal.timeInMillis
            updateLabels()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun pickTime(isStart: Boolean) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = if (isStart) viewModel.startTime else viewModel.endTime
        }
        TimePickerDialog(requireContext(), { _, h, m ->
            cal.set(Calendar.HOUR_OF_DAY, h)
            cal.set(Calendar.MINUTE, m)
            if (isStart) {
                viewModel.startTime = cal.timeInMillis
                if (viewModel.endTime <= viewModel.startTime) {
                    viewModel.endTime = viewModel.startTime + 3600000L // 1 hora después
                }
            } else {
                if (cal.timeInMillis < viewModel.startTime) {
                    Snackbar.make(
                        binding.root,
                        "La hora de término no puede ser anterior a la hora de inicio",
                        Snackbar.LENGTH_LONG
                    ).show()
                } else {
                    viewModel.endTime = cal.timeInMillis
                }
            }
            updateLabels()
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun updateLabels() {
        binding.labelDate.text = df.format(Date(viewModel.reportDate))
        binding.labelStart.text = tf.format(Date(viewModel.startTime))
        binding.labelEnd.text = tf.format(Date(viewModel.endTime))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
