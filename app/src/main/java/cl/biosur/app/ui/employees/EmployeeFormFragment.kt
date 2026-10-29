package cl.biosur.app.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import cl.biosur.app.data.local.entity.EmployeeEntity
import cl.biosur.app.databinding.FragmentEmployeeFormBinding
import cl.biosur.app.util.RutUtils
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Formulario de creación / edición de empleado.
 *
 */
class EmployeeFormFragment : Fragment() {

    private var _binding: FragmentEmployeeFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmployeeViewModel by viewModels()

    private var employeeId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        employeeId = arguments?.getLong("employeeId") ?: -1L
        binding.toolbar.title = if (employeeId == -1L) "Nuevo empleado" else "Editar empleado"
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        if (employeeId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val emp = withContext(Dispatchers.IO) {
                    viewModel.getEmployee(employeeId)
                }
                emp?.let { bind(it) }
            }
        }

        binding.saveButton.setOnClickListener {
            val fullName = binding.nameInput.text.toString().trim()
            val rawRut = binding.rutInput.text.toString().trim()
            val role = binding.roleInput.text.toString().trim()
            val phone = binding.phoneInput.text.toString().trim().ifBlank { null }
            val email = binding.emailInput.text.toString().trim().ifBlank { null }

            if (fullName.isBlank()) {
                binding.nameLayout.error = "El nombre es obligatorio"
                return@setOnClickListener
            } else {
                binding.nameLayout.error = null
            }

            if (rawRut.isBlank()) {
                binding.rutLayout.error = "El RUT es obligatorio"
                return@setOnClickListener
            } else if (!RutUtils.isValidRut(rawRut)) {
                binding.rutLayout.error = "RUT inválido (ej: 12.345.678-5)"
                return@setOnClickListener
            } else {
                binding.rutLayout.error = null
            }

            if (role.isBlank()) {
                binding.roleLayout.error = "El cargo es obligatorio"
                return@setOnClickListener
            } else {
                binding.roleLayout.error = null
            }

            val emp = EmployeeEntity(
                id = if (employeeId == -1L) 0L else employeeId,
                fullName = fullName,
                rut = RutUtils.formatRut(rawRut),
                role = role,
                phone = phone,
                email = email,
                active = binding.activeSwitch.isChecked
            )

            viewModel.createOrUpdate(emp)
            Snackbar.make(view, getString(cl.biosur.app.R.string.employee_saved), Snackbar.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding.cancelButton.setOnClickListener { findNavController().popBackStack() }
    }

    private fun bind(e: EmployeeEntity) {
        binding.nameInput.setText(e.fullName)
        binding.rutInput.setText(e.rut)
        binding.roleInput.setText(e.role)
        binding.phoneInput.setText(e.phone.orEmpty())
        binding.emailInput.setText(e.email.orEmpty())
        binding.activeSwitch.isChecked = e.active
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
