package pt.kitsupixel.kpanime.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import pt.kitsupixel.kpanime.R
import pt.kitsupixel.kpanime.databinding.HomeShowItemBinding
import pt.kitsupixel.kpanime.databinding.ShowItemBinding
import pt.kitsupixel.kpanime.domain.Show


class HomeShowItemAdapter(val clickListener: HomeShowItemClickListener) :
    ListAdapter<Show, HomeShowItemViewHolder>(
        HomeShowItemDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeShowItemViewHolder {
        val withDataBinding: HomeShowItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            HomeShowItemViewHolder.LAYOUT,
            parent,
            false
        )
        return HomeShowItemViewHolder(withDataBinding)
    }

    override fun onBindViewHolder(holder: HomeShowItemViewHolder, position: Int) {
        holder.viewDataBinding.also {
            it.show = getItem(position)
            it.clickListener = clickListener
        }
    }
}


class HomeShowItemClickListener(val clickListener: (showId: Long) -> Unit) {
    fun onClick(show: Show) = clickListener(show.id)
}

class HomeShowItemViewHolder(val viewDataBinding: HomeShowItemBinding) :
    RecyclerView.ViewHolder(viewDataBinding.root) {
    companion object {
        @LayoutRes
        val LAYOUT = R.layout.home_show_item
    }
}

class HomeShowItemDiffCallback :
    DiffUtil.ItemCallback<Show>() {
    override fun areItemsTheSame(oldItem: Show, newItem: Show): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Show, newItem: Show): Boolean {
        return oldItem == newItem
    }
}