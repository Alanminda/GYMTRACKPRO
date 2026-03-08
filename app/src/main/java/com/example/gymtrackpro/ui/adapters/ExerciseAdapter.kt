package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val onClick: (ExerciseEntity) -> Unit
) : ListAdapter<ExerciseEntity, ExerciseAdapter.VH>(Diff) {

    object Diff : DiffUtil.ItemCallback<ExerciseEntity>() {
        override fun areItemsTheSame(old: ExerciseEntity, new: ExerciseEntity) = old.id == new.id
        override fun areContentsTheSame(old: ExerciseEntity, new: ExerciseEntity) = old == new
    }

    inner class VH(val b: ItemExerciseBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemExerciseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvGroup.text = item.muscleGroup
        holder.b.root.setOnClickListener { onClick(item) }
    }

    fun submit(items: List<ExerciseEntity>) = submitList(items)
}
