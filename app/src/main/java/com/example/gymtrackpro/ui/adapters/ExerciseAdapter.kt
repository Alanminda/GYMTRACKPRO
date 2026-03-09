/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/adapters/ExerciseAdapter.kt
 * Proposito: Renderiza listas RecyclerView y maneja interacciones de item.
 */
package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.local.entities.ExerciseEntity
import com.example.gymtrackpro.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val onClick: (ExerciseEntity) -> Unit
) : ListAdapter<ExerciseEntity, ExerciseAdapter.VH>(Diff) {
    // Conjunto de ejercicios marcados como completados en una rutina.
    private var completedExerciseIds: Set<String> = emptySet()

    // [Req C/UI] DiffUtil para RecyclerView optimizada.
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
        // Render de tarjeta ejercicio + estado visual completado/no completado.
        val item = getItem(position)
        holder.b.tvName.text = item.name
        if (completedExerciseIds.contains(item.id)) {
            holder.b.tvGroup.text = "${item.muscleGroup}  |  Hecho"
            val doneIcon = ContextCompat.getDrawable(holder.b.root.context, com.example.gymtrackpro.R.drawable.ic_check_circle_24)
            if (doneIcon != null) {
                val wrapped = DrawableCompat.wrap(doneIcon.mutate())
                DrawableCompat.setTint(
                    wrapped,
                    ContextCompat.getColor(holder.b.root.context, android.R.color.holo_green_dark)
                )
                holder.b.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(wrapped, null, null, null)
                holder.b.tvName.compoundDrawablePadding = 12
            }
            holder.b.root.setCardBackgroundColor(
                ContextCompat.getColor(holder.b.root.context, android.R.color.holo_green_light)
            )
            holder.b.root.strokeColor = ContextCompat.getColor(holder.b.root.context, android.R.color.holo_green_dark)
        } else {
            holder.b.tvGroup.text = item.muscleGroup
            holder.b.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            holder.b.root.setCardBackgroundColor(
                ContextCompat.getColor(holder.b.root.context, android.R.color.white)
            )
            holder.b.root.strokeColor = ContextCompat.getColor(holder.b.root.context, android.R.color.darker_gray)
        }
        holder.b.root.setOnClickListener { onClick(item) }
    }

    fun submit(items: List<ExerciseEntity>) = submitList(items)
    fun submitCompletedIds(ids: Set<String>) {
        // Refresca estado visual "Hecho" al cambiar progreso.
        completedExerciseIds = ids
        notifyDataSetChanged()
    }
    fun getItemAt(position: Int): ExerciseEntity? = currentList.getOrNull(position)
}

