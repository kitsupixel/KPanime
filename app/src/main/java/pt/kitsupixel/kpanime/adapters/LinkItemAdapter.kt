package pt.kitsupixel.kpanime.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.LinkItemBinding
import pt.kitsupixel.kpanime.domain.Link

class LinkItemAdapter(val clickListener: LinkItemClickListener) :
    ListAdapter<Link, LinkItemViewHolder>(
        LinkItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkItemViewHolder {
        val withDataBinding: LinkItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            LinkItemViewHolder.LAYOUT,
            parent,
            false
        )
        return LinkItemViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: LinkItemViewHolder, position: Int) {
        holder.viewDataBinding.also {
            val item = getItem(position)
            it.link = item

            it.typeImageView.setImageResource(when(item.type.toLowerCase()) {
                "magnet" -> R.drawable.ic_magnet_black_24dp
                "torrent" -> R.drawable.ic_torrent_black_24dp
                else -> R.drawable.ic_link_black_24dp
            })

            it.qualityChip.chipBackgroundColor = when (item.quality.toLowerCase()) {
                "480p" -> ColorStateList.valueOf(Color.parseColor("#6610f2"))
                "720p" -> ColorStateList.valueOf(Color.parseColor("#dc3545"))
                else -> ColorStateList.valueOf(Color.parseColor("#007bff"))
            }

            it.linkTypeTextView.text = when (item.type.toLowerCase()) {
                "torrent" -> String.format("S: %d | L: %d", item.seeds, item.leeches)
                else -> item.type
            }

            it.clickListener = clickListener
        }
    }
}


class LinkItemClickListener(val clickListener: (link: Link) -> Unit) {
    fun onClick(link: Link) = clickListener(link)
}

class LinkItemViewHolder(val viewDataBinding: LinkItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.link_item
    }
}

class LinkItemDiffCallback :
    DiffUtil.ItemCallback<Link>() {
    override fun areItemsTheSame(oldItem: Link, newItem: Link): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Link, newItem: Link): Boolean {
        return oldItem == newItem
    }
}