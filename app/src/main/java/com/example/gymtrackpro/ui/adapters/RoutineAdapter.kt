package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.databinding.ItemExerciseBinding

class RoutineAdapter(
    private val onClick: (RoutineEntity) -> Unit
) : ListAdapter<RoutineEntity, RoutineAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<RoutineEntity>() {
        override fun areItemsTheSame(old: RoutineEntity, new: RoutineEntity) = old.id == new.id
        override fun areContentsTheSame(old: RoutineEntity, new: RoutineEntity) = old == new
    }

    inner class VH(val b: ItemExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvGroup.text = "Rutina"
        holder.b.root.setOnClickListener { onClick(item) }
    }

    fun submit(items: List<RoutineEntity>) = submitList(items)
}
