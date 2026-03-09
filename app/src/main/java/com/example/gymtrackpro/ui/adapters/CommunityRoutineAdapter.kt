/**
 * AUTO-DOC: GYMTRACKPRO
 * Archivo: com/example/gymtrackpro/ui/adapters/CommunityRoutineAdapter.kt
 * Proposito: Renderiza listas RecyclerView y maneja interacciones de item.
 */
package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.remote.dto.CommunityRoutineDto
import com.example.gymtrackpro.databinding.ItemCommunityRoutineBinding

class CommunityRoutineAdapter(
    private val onClick: (CommunityRoutineDto) -> Unit,
    private val onToggleFavorite: (CommunityRoutineDto) -> Unit
) : ListAdapter<CommunityRoutineDto, CommunityRoutineAdapter.VH>(Diff) {

    // [Req C/UI] DiffUtil para actualizar solo items cambiados.
    object Diff : DiffUtil.ItemCallback<CommunityRoutineDto>() {
        override fun areItemsTheSame(old: CommunityRoutineDto, new: CommunityRoutineDto): Boolean = old.id == new.id
        override fun areContentsTheSame(old: CommunityRoutineDto, new: CommunityRoutineDto): Boolean = old == new
    }

    inner class VH(val b: ItemCommunityRoutineBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCommunityRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        // [Req C/UI] Render de tarjeta comunitaria.
        val item = getItem(position)
        val exerciseCount = if (item.exerciseCount > 0) {
            item.exerciseCount
        } else {
            item.exerciseIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct().size
        }
        val duration = item.durationMinutes ?: parseDurationFromName(item.name)
        holder.b.tvName.text = item.name
        holder.b.tvOwner.text = "Creador: ${item.ownerName}"
        holder.b.tvExercises.text = "Ejercicios: $exerciseCount"
        holder.b.tvDuration.text = "Tiempo: ${duration?.let { "$it min" } ?: "N/D"}"
        holder.b.tvFavorites.text = "Favoritos: ${item.favoritesCount}"
        val ctx = holder.b.root.context
        if (item.isFavorite) {
            // Estado visual de favorito activo.
            holder.b.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on)
            holder.b.btnFavorite.setColorFilter(ContextCompat.getColor(ctx, android.R.color.holo_orange_dark))
            holder.b.btnFavorite.alpha = 1f
        } else {
            holder.b.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off)
            holder.b.btnFavorite.setColorFilter(ContextCompat.getColor(ctx, android.R.color.darker_gray))
            holder.b.btnFavorite.alpha = 0.9f
        }
        holder.b.btnFavorite.setOnClickListener { onToggleFavorite(item) }
        // Click de tarjeta abre detalle de rutina publica.
        holder.b.root.setOnClickListener { onClick(item) }
    }

    private fun parseDurationFromName(name: String): Int? {
        val match = Regex("\\((\\d+)\\s*min\\)", RegexOption.IGNORE_CASE).find(name)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }
}

