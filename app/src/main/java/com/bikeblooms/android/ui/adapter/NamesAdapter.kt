package com.bikeblooms.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bikeblooms.android.databinding.NameItemBinding
import com.bikeblooms.android.model.Brand
import com.bikeblooms.android.ui.adapter.NamesAdapter.CustomViewHolder

class NamesAdapter(var callback: (name: Brand) -> Unit) :
    ListAdapter<Brand, CustomViewHolder>(SampleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CustomViewHolder {
        val binding = NameItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)

    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.bindTo(getItem(position), callback)
    }

    class CustomViewHolder(var binding: NameItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(brand: Brand, itemCallback: (Brand) -> Unit) {
            binding.tvName.text = brand.name
            binding.root.setOnClickListener {
                itemCallback.invoke(brand)
            }
        }
    }

    class SampleItemDiffCallback : DiffUtil.ItemCallback<Brand>() {
        override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean =
            oldItem == newItem

    }

}