package cl.biosur.app.ui.report

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
import cl.biosur.app.MainActivity
import cl.biosur.app.R
import cl.biosur.app.databinding.FragmentReportListBinding
import cl.biosur.app.util.OfflineManager
import kotlinx.coroutines.launch

/**
 * Lista de informes.
 * Responsabilidad: Compañero 2.
 */
class ReportListFragment : Fragment() {

    private var _binding: FragmentReportListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels()
    private lateinit var adapter: ReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    (activity as? MainActivity)?.logout()
                    true
                }
                else -> false
            }
        }

        adapter = ReportAdapter { item ->
            val dir = ReportListFragmentDirections.actionListToDetail(item.report.id)
            findNavController().navigate(dir)
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_form)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reports.collect { list ->
                    adapter.submitList(list)
                    binding.emptyState.visibility =
                        if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        OfflineManager.isOnline.observe(viewLifecycleOwner) { online ->
            binding.offlineBadge.visibility = if (!online) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
