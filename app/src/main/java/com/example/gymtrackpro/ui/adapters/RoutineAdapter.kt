package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
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
    private companion object {
        const val PAYLOAD_ACTION_STATE = "payload_action_state"
    }

    var revealWidthPx: Float = 0f
        private set

    private var openedPosition: Int = NO_POSITION

    object Diff : DiffUtil.ItemCallback<RoutineEntity>() {
        override fun areItemsTheSame(old: RoutineEntity, new: RoutineEntity) = old.id == new.id
        override fun areContentsTheSame(old: RoutineEntity, new: RoutineEntity) = old == new
    }

    inner class VH(val b: ItemRoutineBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemRoutineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (revealWidthPx <= 0f) {
            val density = parent.resources.displayMetrics.density
            revealWidthPx = 168f * density
        }
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvGroup.text = "Rutina"
        holder.b.foregroundContainer.translationX = if (openedPosition == position) -revealWidthPx else 0f

        holder.b.foregroundContainer.setOnClickListener {
            if (openedPosition == position) {
                closeActions()
            } else {
                onClick(item)
            }
        }
        holder.b.btnDelete.setOnClickListener {
            closeActions()
            onDelete(item)
        }
        holder.b.btnShare.setOnClickListener {
            closeActions()
            onShare(item)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(PAYLOAD_ACTION_STATE)) {
            holder.b.foregroundContainer.animate()
                .translationX(if (openedPosition == position) -revealWidthPx else 0f)
                .setDuration(120)
                .start()
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun submit(items: List<RoutineEntity>) = submitList(items)

    fun isActionsOpen(position: Int): Boolean = openedPosition == position

    fun toggleActions(position: Int) {
        val previous = openedPosition
        openedPosition = if (openedPosition == position) NO_POSITION else position
        if (previous != NO_POSITION) notifyItemChanged(previous, PAYLOAD_ACTION_STATE)
        if (position != NO_POSITION) notifyItemChanged(position, PAYLOAD_ACTION_STATE)
    }

    fun closeActions() {
        if (openedPosition == NO_POSITION) return
        val previous = openedPosition
        openedPosition = NO_POSITION
        notifyItemChanged(previous, PAYLOAD_ACTION_STATE)
    }
}
