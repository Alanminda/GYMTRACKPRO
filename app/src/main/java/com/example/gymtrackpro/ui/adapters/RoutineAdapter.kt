package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.local.entities.RoutineEntity
import com.example.gymtrackpro.databinding.ItemRoutineBinding

class RoutineAdapter(
    private val onClick: (RoutineEntity) -> Unit,
    private val onDelete: (RoutineEntity) -> Unit,
    private val onShare: (RoutineEntity) -> Unit
) : ListAdapter<RoutineEntity, RoutineAdapter.VH>(Diff) {
    var revealWidthPx: Float = 0f
        private set

    object Diff : DiffUtil.ItemCallback<RoutineEntity>() {
        override fun areItemsTheSame(old: RoutineEntity, new: RoutineEntity) = old.id == new.id
        override fun areContentsTheSame(old: RoutineEntity, new: RoutineEntity) = old == new
    }

    inner class VH(val b: ItemRoutineBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (revealWidthPx <= 0f) {
            val density = parent.resources.displayMetrics.density
            revealWidthPx = 84f * density
        }
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.translationX = 0f
        val item = getItem(position)
        val duration = parseDurationFromName(item.name)
        holder.b.tvName.text = item.name
        if (item.routineType == "FAVORITE") {
            holder.b.tvGroup.text = "Favorita de ${item.ownerName ?: "Comunidad"}  |  ${duration?.let { "$it min" } ?: "Tiempo N/D"}"
            holder.b.foregroundContainer.strokeColor =
                ContextCompat.getColor(holder.b.root.context, android.R.color.holo_orange_dark)
        } else {
            holder.b.tvGroup.text = "Rutina propia  |  ${duration?.let { "$it min" } ?: "Tiempo N/D"}"
            holder.b.foregroundContainer.strokeColor =
                ContextCompat.getColor(holder.b.root.context, android.R.color.holo_green_dark)
        }
        applyRevealVisualState(holder, position)

        holder.b.foregroundContainer.setOnClickListener {
            onClick(item)
        }
        holder.b.btnDelete.setOnClickListener {
            onDelete(item)
        }
        holder.b.btnShare.setOnClickListener {
            onShare(item)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        holder.itemView.translationX = 0f
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        holder.itemView.translationX = 0f
        holder.b.foregroundContainer.translationX = 0f
        holder.b.btnDelete.alpha = 1f
        holder.b.btnShare.alpha = 1f
    }

    fun submit(items: List<RoutineEntity>) = submitList(items)
    fun getItemAt(position: Int): RoutineEntity? = currentList.getOrNull(position)

    fun isActionsOpen(position: Int): Boolean = false
    fun getOpenedDirection(position: Int): Int = 0
    fun openActions(position: Int, direction: Int) {}
    fun closeActions() {}
    fun resetSwipe(position: Int) {}

    private fun parseDurationFromName(name: String): Int? {
        val match = Regex("\\((\\d+)\\s*min\\)", RegexOption.IGNORE_CASE).find(name)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun applyRevealVisualState(holder: VH, position: Int) {
        holder.b.foregroundContainer.translationX = 0f
        holder.b.btnDelete.alpha = 1f
        holder.b.btnShare.alpha = 1f
    }
}
