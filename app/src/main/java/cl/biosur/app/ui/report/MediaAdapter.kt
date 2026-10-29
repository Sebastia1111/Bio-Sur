package cl.biosur.app.ui.report

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.biosur.app.R
import cl.biosur.app.data.local.entity.MediaEntity
import cl.biosur.app.databinding.ItemMediaBinding
import com.bumptech.glide.Glide
import java.io.File

class MediaAdapter(
    private val onDelete: (MediaEntity) -> Unit
) : ListAdapter<MediaEntity, MediaAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemMediaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemMediaBinding) :
        RecyclerView.ViewHolder(b.root) {
        fun bind(m: MediaEntity) {
            b.mediaType.text = m.type
            b.mediaComment.text = m.comment ?: "Sin comentario"
            b.btnDelete.setOnClickListener { onDelete(m) }

            val context = b.root.context
            val model: Any = when {
                m.filePath.startsWith("content://") -> Uri.parse(m.filePath)
                m.filePath.isNotBlank() && File(m.filePath).exists() -> File(m.filePath)
                else -> R.drawable.ic_camera
            }

            Glide.with(context)
                .load(model)
                .placeholder(R.drawable.ic_camera)
                .error(R.drawable.ic_camera)
                .centerCrop()
                .into(b.mediaThumb)
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MediaEntity>() {
            override fun areItemsTheSame(o: MediaEntity, n: MediaEntity) = o.id == n.id
            override fun areContentsTheSame(o: MediaEntity, n: MediaEntity) = o == n
        }
    }
}
