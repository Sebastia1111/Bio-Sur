package cl.biosur.app.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.biosur.app.data.local.relation.ReportWithDetails
import cl.biosur.app.databinding.ItemReportBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportAdapter(
    private val onClick: (ReportWithDetails) -> Unit
) : ListAdapter<ReportWithDetails, ReportAdapter.VH>(DIFF) {

    private val df = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    private val tf = SimpleDateFormat("HH:mm", Locale("es", "CL"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemReportBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemReportBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: ReportWithDetails) {
            val r = item.report
            b.reportDate.text = df.format(Date(r.date))
            b.reportTimeRange.text = "${tf.format(Date(r.startTime))} – ${tf.format(Date(r.endTime))}"
            b.reportLocation.text = r.address ?: r.latitude?.let { "%.5f, %.5f".format(it, r.longitude) } ?: "—"
            b.reportCounts.text =
                "${item.materials.size} mat · ${item.equipment.size} eq · ${item.personnel.size} pers · ${item.media.size} media"
            b.reportSupervisor.text = "Supervisor #${r.supervisorId}"
            if (r.synced) {
                b.syncBadge.text = "✓ Sincronizado"
                b.syncBadge.setBackgroundResource(0)
            } else {
                b.syncBadge.text = "Pendiente"
            }
            b.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ReportWithDetails>() {
            override fun areItemsTheSame(o: ReportWithDetails, n: ReportWithDetails) = o.report.id == n.report.id
            override fun areContentsTheSame(o: ReportWithDetails, n: ReportWithDetails) = o == n
        }
    }
}
