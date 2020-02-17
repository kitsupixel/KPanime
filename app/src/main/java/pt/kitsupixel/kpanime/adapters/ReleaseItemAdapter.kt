package pt.kitsupixel.kpanime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.ReleaseItemBinding
import pt.kitsupixel.kpanime.domain.Episode
import pt.kitsupixel.kpanime.domain.EpisodeAndShow
import pt.kitsupixel.kpanime.domain.Show

class ReleaseItemAdapter(val clickListener: ReleaseItemClickListener) :
    ListAdapter<EpisodeAndShow, ReleaseItemViewHolder>(
        ReleaseItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReleaseItemViewHolder {
        val withDataBinding: ReleaseItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ReleaseItemViewHolder.LAYOUT,
            parent,
            false
        )
        return ReleaseItemViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ReleaseItemViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.episodeAndShow = getItem(position)
            it.clickListener = clickListener
        }
    }
}


class ReleaseItemClickListener(val clickListener: (showId: Long, episodeId: Long) -> Unit) {
    fun onClick(showId: Long, episodeId: Long) = clickListener(showId, episodeId)
}

class ReleaseItemViewHolder(val viewDataBinding: ReleaseItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.release_item
    }
}

class ReleaseItemDiffCallback :
    DiffUtil.ItemCallback<EpisodeAndShow>() {
    override fun areItemsTheSame(oldItem: EpisodeAndShow, newItem: EpisodeAndShow): Boolean {
        return oldItem.episode.id == newItem.episode.id
    }

    override fun areContentsTheSame(oldItem: EpisodeAndShow, newItem: EpisodeAndShow): Boolean {
        return oldItem == newItem
    }
}