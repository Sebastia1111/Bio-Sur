package cl.biosur.app.ui.report

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import cl.biosur.app.R
import cl.biosur.app.databinding.FragmentReportFormBinding
import cl.biosur.app.ui.report.steps.StepCommentsFragment
import cl.biosur.app.ui.report.steps.StepDateTimeFragment
import cl.biosur.app.ui.report.steps.StepItemsFragment
import cl.biosur.app.ui.report.steps.StepMediaFragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

/**
 * Formulario de informe con wizard de 6 pasos.
 * Responsabilidad: Compañero 2.
 *
 * Pasos:
 *   1. Fecha y horas (inicio/término)
 *   2. Multimedia + GPS
 *   3. Materiales
 *   4. Equipos
 *   5. Personal
 *   6. Comentarios finales + Guardar
 */
class ReportFormFragment : Fragment() {

    private var _binding: FragmentReportFormBinding? = null
    private val binding get() = _binding!!
    // Scope compartido con los fragmentos de cada paso para consolidar el borrador
    private val viewModel: ReportViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val steps = listOf(
            StepDateTimeFragment(),
            StepMediaFragment(),
            StepItemsFragment.newInstance(StepItemsFragment.TYPE_MATERIALS),
            StepItemsFragment.newInstance(StepItemsFragment.TYPE_EQUIPMENT),
            StepItemsFragment.newInstance(StepItemsFragment.TYPE_PERSONNEL),
            StepCommentsFragment()
        )

        binding.pager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = steps.size
            override fun createFragment(position: Int) = steps[position]
        }

        TabLayoutMediator(binding.stepper, binding.pager) { tab, pos ->
            tab.text = "${pos + 1}"
        }.attach()

        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val isLast = position == steps.lastIndex
                binding.nextButton.visibility = if (isLast) View.GONE else View.VISIBLE
                binding.backButton.visibility = if (position == 0) View.GONE else View.VISIBLE
                binding.saveButton.visibility = if (isLast) View.VISIBLE else View.GONE
            }
        })

        binding.nextButton.setOnClickListener {
            binding.pager.currentItem = binding.pager.currentItem + 1
        }
        binding.backButton.setOnClickListener {
            if (binding.pager.currentItem > 0) {
                binding.pager.currentItem = binding.pager.currentItem - 1
            } else {
                findNavController().popBackStack()
            }
        }

        binding.saveButton.setOnClickListener {
            if (viewModel.endTime <= viewModel.startTime) {
                Snackbar.make(view, "La hora de término debe ser posterior a la hora de inicio", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveReport(
                onSaved = {
                    Snackbar.make(view, getString(R.string.report_saved), Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
