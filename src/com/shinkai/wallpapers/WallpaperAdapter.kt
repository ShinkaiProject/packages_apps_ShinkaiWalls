package com.shinkai.wallpapers

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class WallpaperAdapter(
    private val items: List<Wallpaper>,
    private val onClick: (Wallpaper) -> Unit
) : RecyclerView.Adapter<WallpaperAdapter.VH>() {

    class VH(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wallpaper, parent, false) as ImageView
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.image.context.assets.open(item.assetPath).use { input ->
            val opts = BitmapFactory.Options().apply { inSampleSize = 4 }
            holder.image.setImageBitmap(BitmapFactory.decodeStream(input, null, opts))
        }
        holder.image.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
