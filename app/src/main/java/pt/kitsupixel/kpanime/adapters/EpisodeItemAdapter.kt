package pt.kitsupixel.kpanime.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.EpisodeItemBinding
import pt.kitsupixel.kpanime.domain.Episode

class EpisodeItemAdapter(val clickListener: EpisodeItemClickListener,
                         val downloadClickListener: EpisodeItemDownloadClickListener,
                         val watchedClickListener: EpisodeItemWatchedClickListener) :
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
            val tempEpisode = getItem(position)
            it.episode = tempEpisode
            it.clickListener = clickListener
            it.downloadClickListener = downloadClickListener
            it.watchedClickListener = watchedClickListener


            it.downloadImageButton.setColorFilter(when(tempEpisode.downloaded) {
                true -> android.R.color.holo_green_dark
                else -> android.R.color.black
            })

            it.watchedImageButton.setColorFilter(when(tempEpisode.watched) {
                true -> android.R.color.holo_blue_dark
                else -> android.R.color.black
            })
        }
    }
}


class EpisodeItemClickListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(episode: Episode) = clickListener(episode.id)
}

class EpisodeItemDownloadClickListener(val clickListener: (id: Long) -> Unit) {
    fun onClick(episode: Episode) = clickListener(episode.id)
}

class EpisodeItemWatchedClickListener(val clickListener: (id: Long) -> Unit) {
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