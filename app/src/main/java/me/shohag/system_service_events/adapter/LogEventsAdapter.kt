package me.shohag.system_service_events.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.shohag.system_service_events.databinding.ItemLogBinding
import me.shohag.system_service_events.model.LogModel

class LogEventsAdapter  :
    ListAdapter<LogModel, LogEventsAdapter.ViewHolder>(DiffCallback){

    class ViewHolder private constructor(private val binding: ItemLogBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(log: LogModel) {
            binding.log = log
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val view = ItemLogBinding.inflate(inflater, parent, false)
                return ViewHolder(view)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LogModel>() {
        override fun areItemsTheSame(
            oldItem: LogModel,
            newItem: LogModel
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: LogModel,
            newItem: LogModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

    }
}