package pt.kitsupixel.kpanime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.ShowItemBinding
import pt.kitsupixel.kpanime.domain.Show


class ShowItemAdapter(val clickListener: ShowItemClickListener) :
    ListAdapter<Show, ShowItemViewHolder>(
        ShowItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowItemViewHolder {
        val withDataBinding: ShowItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            ShowItemViewHolder.LAYOUT,
            parent,
            false
        )
        return ShowItemViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: ShowItemViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = getItem(position)
            it.clickListener = clickListener
        }
    }

    fun setSearchResult(filteredList: List<Show>?) {
        submitList(filteredList)
    }
}


class ShowItemClickListener(val clickListener: (showId: Long) -> Unit) {
    fun onClick(show: Show) = clickListener(show.id)
}

class ShowItemViewHolder(val viewDataBinding: ShowItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.show_item
    }
}

class ShowItemDiffCallback :
    DiffUtil.ItemCallback<Show>() {
    override fun areItemsTheSame(oldItem: Show, newItem: Show): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Show, newItem: Show): Boolean {
        return oldItem == newItem
    }
}