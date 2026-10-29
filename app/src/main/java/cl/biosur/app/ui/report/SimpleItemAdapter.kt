package cl.biosur.app.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.biosur.app.databinding.ItemSimpleBinding
import cl.biosur.app.ui.report.steps.StepItemsFragment

/**
 * Adapter genérico para items simples (materiales, equipos, personal).
 */
class SimpleItemAdapter(
    private val onDelete: (Any) -> Unit
) : ListAdapter<StepItemsFragment.SimpleItem, SimpleItemAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSimpleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemSimpleBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(item: StepItemsFragment.SimpleItem) {
            b.itemName.text = item.name
            b.itemSubtitle.text = item.subtitle
            b.btnDelete.setOnClickListener { onDelete(item.raw) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<StepItemsFragment.SimpleItem>() {
            override fun areItemsTheSame(o: StepItemsFragment.SimpleItem, n: StepItemsFragment.SimpleItem) = o.id == n.id
            override fun areContentsTheSame(o: StepItemsFragment.SimpleItem, n: StepItemsFragment.SimpleItem) = o == n
        }
    }
}
