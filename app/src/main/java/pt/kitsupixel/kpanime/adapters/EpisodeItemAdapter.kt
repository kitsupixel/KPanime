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
import pt.kitsupixel.kpanime.domain.Episode

class EpisodeItemAdapter(val clickListener: EpisodeItemClickListener) :
    ListAdapter<Episode, EpisodeItemViewHolder>(
        EpisodeItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeItemViewHolder {
        val withDataBinding: EpisodeItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            EpisodeItemViewHolder.LAYOUT,
            parent,
            false
        )
        return EpisodeItemViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: EpisodeItemViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.episode = getItem(position)
            it.clickListener = clickListener
        }
    }

    fun setSearchResult(filteredList: List<Episode>?) {
        submitList(filteredList)
    }
}


class EpisodeItemClickListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(episode: Episode) = clickListener(episode.id)
}

class EpisodeItemViewHolder(val viewDataBinding: EpisodeItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.episode_item
    }
}

class EpisodeItemDiffCallback :
    DiffUtil.ItemCallback<Episode>() {
    override fun areItemsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Episode, newItem: Episode): Boolean {
        return oldItem == newItem
    }
}