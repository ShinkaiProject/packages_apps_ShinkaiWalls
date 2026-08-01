package com.shinkai.wallpapers

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WallpaperAdapter(
    private val items: List<Wallpaper>,
    private val onClick: (Wallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        val image: ImageView = view.findViewById(R.id.wallpaper_image)
        val label: TextView = view.findViewById(R.id.wallpaper_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.label.text = item.name
        holder.image.context.assets.open(item.assetPath).use { input ->
            val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
            holder.image.setImageBitmap(BitmapFactory.decodeStream(input, null, opts))
        }
        holder.image.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
