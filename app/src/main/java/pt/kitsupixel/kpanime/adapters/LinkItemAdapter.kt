package pt.kitsupixel.kpanime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.EpisodeItemBinding
import pt.kitsupixel.kpanime.databinding.LinkItemBinding
import pt.kitsupixel.kpanime.domain.Episode
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
            it.link = getItem(position)
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