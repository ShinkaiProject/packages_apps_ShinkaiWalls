package com.shinkai.wallpapers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class WallCategoryAdapter(
    private val onClick: (WallCategory) -> Unit
) : RecyclerView.Adapter<WallCategoryAdapter.VH>() {

    private val items = mutableListOf<WallCategory>()

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.category_card)
        val image: ImageView = view.findViewById(R.id.category_image)
        val label: TextView = view.findViewById(R.id.category_title)
    }

    fun submit(categories: List<WallCategory>) {
        val previousSize = items.size
        items.clear()
        items.addAll(categories)
        notifyItemRangeRemoved(0, previousSize)
        notifyItemRangeInserted(0, items.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wall_category, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.label.text = item.title
        holder.card.contentDescription = context.getString(
            R.string.category_card_description,
            item.title,
            context.resources.getQuantityString(R.plurals.walls_count, item.count, item.count)
        )

        item.previewUrl?.let { previewUrl ->
            ImageLoader.load(
                context = context,
                imageUrl = previewUrl,
                imageView = holder.image,
                targetWidth = 300,
                targetHeight = 500
            )
        }

        holder.card.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
