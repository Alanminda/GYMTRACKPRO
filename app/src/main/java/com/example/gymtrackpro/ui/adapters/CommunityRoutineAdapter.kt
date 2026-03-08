package com.example.gymtrackpro.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymtrackpro.data.remote.dto.CommunityRoutineDto
import com.example.gymtrackpro.databinding.ItemCommunityRoutineBinding

class CommunityRoutineAdapter(
    private val onClick: (CommunityRoutineDto) -> Unit,
    private val onToggleFavorite: (CommunityRoutineDto) -> Unit
) : ListAdapter<CommunityRoutineDto, CommunityRoutineAdapter.VH>(Diff) {

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
        val item = getItem(position)
        holder.b.tvName.text = item.name
        holder.b.tvMeta.text = "${item.ownerName}  |  ${item.exerciseIds.size} ejercicios  |  ${item.favoritesCount} favoritos"
        holder.b.btnFavorite.setImageResource(
            if (item.isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )
        holder.b.btnFavorite.setOnClickListener { onToggleFavorite(item) }
        holder.b.root.setOnClickListener { onClick(item) }
    }
}
