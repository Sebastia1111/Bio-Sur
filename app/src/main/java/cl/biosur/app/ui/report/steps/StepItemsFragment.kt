package cl.biosur.app.ui.report.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import cl.biosur.app.data.local.entity.EquipmentEntity
import cl.biosur.app.data.local.entity.MaterialEntity
import cl.biosur.app.data.local.entity.PersonnelEntity
import cl.biosur.app.databinding.StepReportItemsBinding
import cl.biosur.app.ui.report.ReportViewModel
import cl.biosur.app.ui.report.SimpleItemAdapter
import kotlinx.coroutines.launch

/**
 * Pasos 3, 4, 5: Materiales / Equipos / Personal.
 * Reutiliza el mismo layout [step_report_items] con tipo variable.
 *
 * Responsabilidad: Compañero 2.
 */
class StepItemsFragment : Fragment() {

    private var _binding: StepReportItemsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by activityViewModels()

    private val type: String by lazy { arguments?.getString(ARG_TYPE) ?: TYPE_MATERIALS }
    private lateinit var adapter: SimpleItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = StepReportItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (type) {
            TYPE_MATERIALS -> {
                binding.sectionTitle.text = getString(cl.biosur.app.R.string.report_field_materials)
                binding.extraLayout.hint = getString(cl.biosur.app.R.string.material_unit)
            }
            TYPE_EQUIPMENT -> {
                binding.sectionTitle.text = getString(cl.biosur.app.R.string.report_field_equipment)
                binding.extraLayout.hint = "Notas"
            }
            TYPE_PERSONNEL -> {
                binding.sectionTitle.text = getString(cl.biosur.app.R.string.report_field_personnel)
                binding.extraLayout.hint = getString(cl.biosur.app.R.string.personnel_role)
            }
        }

        adapter = SimpleItemAdapter { item -> removeItem(item) }
        binding.itemsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.itemsRecycler.adapter = adapter

        binding.btnAdd.setOnClickListener { addItem() }

        observeItems()
    }

    private fun addItem() {
        val name = binding.nameInput.text.toString().trim()
        val qty = binding.quantityInput.text.toString().trim().ifBlank { "1" }
        val extra = binding.extraInput.text.toString().trim()
        if (name.isBlank()) return

        when (type) {
            TYPE_MATERIALS -> viewModel.addMaterial(
                MaterialEntity(reportId = 0, name = name, quantity = qty.toDoubleOrNull() ?: 1.0, unit = extra.ifBlank { "un" })
            )
            TYPE_EQUIPMENT -> viewModel.addEquipment(
                EquipmentEntity(reportId = 0, name = name, quantity = qty.toIntOrNull() ?: 1, notes = extra.ifBlank { null })
            )
            TYPE_PERSONNEL -> viewModel.addPersonnel(
                PersonnelEntity(reportId = 0, employeeId = null, name = name, functionInWork = extra.ifBlank { "Sin asignar" })
            )
        }

        binding.nameInput.text?.clear()
        binding.quantityInput.text?.clear()
        binding.extraInput.text?.clear()
        binding.nameInput.requestFocus()
    }

    private fun removeItem(item: Any) {
        when (type) {
            TYPE_MATERIALS -> (item as? MaterialEntity)?.let { viewModel.removeMaterial(it) }
            TYPE_EQUIPMENT -> (item as? EquipmentEntity)?.let { viewModel.removeEquipment(it) }
            TYPE_PERSONNEL -> (item as? PersonnelEntity)?.let { viewModel.removePersonnel(it) }
        }
    }

    private fun observeItems() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                when (type) {
                    TYPE_MATERIALS -> viewModel.draftMaterials.collect { list ->
                        adapter.submitList(list.map { SimpleItem(it.id, it.name, "${it.quantity} ${it.unit}", it) })
                    }
                    TYPE_EQUIPMENT -> viewModel.draftEquipment.collect { list ->
                        adapter.submitList(list.map { SimpleItem(it.id, it.name, "${it.quantity} ${it.notes ?: ""}".trim(), it) })
                    }
                    TYPE_PERSONNEL -> viewModel.draftPersonnel.collect { list ->
                        adapter.submitList(list.map { SimpleItem(it.id, it.name, it.functionInWork, it) })
                    }
                }
            }
        }
    }

    data class SimpleItem(val id: Long, val name: String, val subtitle: String, val raw: Any)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TYPE_MATERIALS = "MATERIALS"
        const val TYPE_EQUIPMENT = "EQUIPMENT"
        const val TYPE_PERSONNEL = "PERSONNEL"
        private const val ARG_TYPE = "type"

        fun newInstance(type: String) = StepItemsFragment().apply {
            arguments = Bundle().apply { putString(ARG_TYPE, type) }
        }
    }
}
