package cl.biosur.app.ui.report.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import cl.biosur.app.databinding.StepReportCommentsBinding
import cl.biosur.app.ui.report.ReportViewModel

/**
 * Paso 6: Comentarios finales + (en la pantalla contenedora) botón Guardar.
 * Responsabilidad: Compañero 2.
 */
class StepCommentsFragment : Fragment() {

    private var _binding: StepReportCommentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = StepReportCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.commentsInput.setText(viewModel.comments)
        binding.commentsInput.doAfterTextChanged {
            viewModel.comments = it?.toString().orEmpty()
        }
    }

    override fun onDestroyView() {
        viewModel.comments = binding.commentsInput.text.toString()
        super.onDestroyView()
        _binding = null
    }
}
