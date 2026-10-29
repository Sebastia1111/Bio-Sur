package cl.biosur.app.ui.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cl.biosur.app.MainActivity
import cl.biosur.app.R
import cl.biosur.app.databinding.FragmentCompanyDashboardBinding
import cl.biosur.app.ui.report.ReportAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * Panel de empresa (rol COMPANY).
 *
 * Muestra:
 *  - Contadores (informes, supervisores, empleados, pendientes).
 *  - Lista de TODOS los informes recientes subidos por TODOS los supervisores.
 *  - Creación de cuentas de supervisor para la empresa.
 *  - Acceso a detalle + exportación de cada informe.
 */
class CompanyDashboardFragment : Fragment() {

    private var _binding: FragmentCompanyDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CompanyDashboardViewModel by viewModels()
    private lateinit var adapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.settingsFragment)
                    true
                }
                R.id.action_logout -> {
                    (activity as? MainActivity)?.logout()
                    true
                }
                else -> false
            }
        }

        adapter = ReportAdapter { item ->
            val dir = CompanyDashboardFragmentDirections.actionDashboardToReportDetail(item.report.id)
            findNavController().navigate(dir)
        }
        binding.reportsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.reportsRecycler.adapter = adapter

        binding.btnCreateSupervisor.setOnClickListener {
            showCreateSupervisorDialog()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { s ->
                    binding.statTotalReports.text = s.totalReports.toString()
                    binding.statSupervisors.text = s.totalSupervisors.toString()
                    binding.statEmployees.text = s.activeEmployees.toString()
                    binding.statPending.text = s.pendingSync.toString()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recentReports.collect { list ->
                    adapter.submitList(list)
                }
            }
        }
    }

    private fun showCreateSupervisorDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        }

        val nameLayout = TextInputLayout(context).apply { hint = "Nombre completo del supervisor" }
        val nameInput = TextInputEditText(context)
        nameLayout.addView(nameInput)

        val emailLayout = TextInputLayout(context).apply {
            hint = "Correo electrónico (ej: supervisor@biosur.cl)"
            val mt = (8 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = mt }
        }
        val emailInput = TextInputEditText(context)
        emailLayout.addView(emailInput)

        val passLayout = TextInputLayout(context).apply {
            hint = "Contraseña (mínimo 6 caracteres)"
            val mt = (8 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = mt }
        }
        val passInput = TextInputEditText(context).apply {
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passLayout.addView(passInput)

        layout.addView(nameLayout)
        layout.addView(emailLayout)
        layout.addView(passLayout)

        MaterialAlertDialogBuilder(context)
            .setTitle("Crear cuenta de supervisor")
            .setView(layout)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton("Crear") { _, _ ->
                val name = nameInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val pass = passInput.text.toString().trim()

                if (name.isBlank() || email.isBlank() || pass.length < 6) {
                    Snackbar.make(binding.root, "Completa todos los campos (contraseña mín 6 caracteres)", Snackbar.LENGTH_LONG).show()
                    return@setPositiveButton
                }

                viewModel.createSupervisor(
                    email = email,
                    password = pass,
                    fullName = name,
                    onSuccess = {
                        Snackbar.make(binding.root, "Supervisor creado exitosamente", Snackbar.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Snackbar.make(binding.root, err, Snackbar.LENGTH_LONG).show()
                    }
                )
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
