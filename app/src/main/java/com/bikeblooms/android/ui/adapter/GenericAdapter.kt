package com.bikeblooms.android.ui.adapter

import android.content.Context
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.atomic.AtomicInteger

class GenericAdapter<T : Any>(
    private val context: Context,
    private val layoutResIds: SparseIntArray,
    private val bindViewHolder: (View, T, Int) -> Unit,
    private val itemClick: (T) -> Unit,
    private val useSingleton: Boolean = true
) : RecyclerView.Adapter<GenericAdapter<T>.ViewHolder>() {

    private var itemList: List<T> = emptyList()
    private val viewTypeCounter = AtomicInteger(0)
    private val viewTypeMap = HashMap<Class<*>, Int>()

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): GenericAdapter<T>.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val layoutResIds = if (layoutResIds.size() == 1) {
            layoutResIds.valueAt(0)
        } else {
            layoutResIds.get(viewType)
        }

        val itemView = inflater.inflate(layoutResIds, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GenericAdapter<T>.ViewHolder, position: Int) {
        val item = itemList[position]
        val viewType = getItemViewType(position)
        holder.bind(item, viewType)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                val item = itemList.getOrNull(position)
                item?.let(itemClick)
            }
        }

        fun bind(item: T, viewType: Int) {
            bindViewHolder(itemView, item, viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
        val item = itemList[position]
        val itemClass = item.javaClass
        return viewTypeMap.getOrPut(itemClass) {
            viewTypeCounter.andIncrement
        }
    }

    fun setItem(items: List<T>) {
        val diffCallback = DefaultDiffCallback(itemList, items)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        itemList = items
        diffResult.dispatchUpdatesTo(this)
    }

    private class DefaultDiffCallback<T : Any>(
        private val oldList: List<T>, private val newList: List<T>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(
            oldItemPosition: Int, newItemPosition: Int
        ): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(
            oldItemPosition: Int, newItemPosition: Int
        ): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }
}