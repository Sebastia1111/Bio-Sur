package cl.biosur.app.ui.employees

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cl.biosur.app.R
import cl.biosur.app.data.local.entity.EmployeeEntity
import cl.biosur.app.databinding.FragmentEmployeeListBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Lista de empleados (CRUD).
 *
 * Acceso: tab "Empleados" del bottom nav.
 */
class EmployeeListFragment : Fragment() {

    private var _binding: FragmentEmployeeListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmployeeViewModel by viewModels()
    private lateinit var adapter: EmployeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmployeeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EmployeeAdapter(
            onClick = { emp ->
                val dir = EmployeeListFragmentDirections.actionListToEmployeeForm(emp.id)
                findNavController().navigate(dir)
            },
            onLongClick = { emp -> confirmDelete(emp) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_employee_form)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.employees.collect { list ->
                    adapter.submitList(list)
                    binding.emptyState.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun confirmDelete(emp: EmployeeEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar empleado")
            .setMessage("¿Eliminar a ${emp.fullName}?")
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ -> viewModel.delete(emp) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
