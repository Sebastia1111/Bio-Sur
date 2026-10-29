package cl.biosur.app.ui.employees

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.biosur.app.data.local.entity.EmployeeEntity
import cl.biosur.app.databinding.ItemEmployeeBinding

class EmployeeAdapter(
    private val onClick: (EmployeeEntity) -> Unit,
    private val onLongClick: (EmployeeEntity) -> Unit
) : ListAdapter<EmployeeEntity, EmployeeAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEmployeeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemEmployeeBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(e: EmployeeEntity) {
            b.name.text = e.fullName
            b.role.text = e.role
            b.rut.text = e.rut
            b.initials.text = e.fullName.split(" ")
                .take(2)
                .joinToString("") { it.firstOrNull()?.toString() ?: "" }
                .uppercase()
            b.root.setOnClickListener { onClick(e) }
            b.root.setOnLongClickListener { onLongClick(e); true }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<EmployeeEntity>() {
            override fun areItemsTheSame(o: EmployeeEntity, n: EmployeeEntity) = o.id == n.id
            override fun areContentsTheSame(o: EmployeeEntity, n: EmployeeEntity) = o == n
        }
    }
}
