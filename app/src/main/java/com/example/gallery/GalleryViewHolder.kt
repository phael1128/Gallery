package com.example.gallery

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.gallery.databinding.ItemGalleryBinding

class GalleryViewHolder(
    private val binding: ItemGalleryBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(glide: RequestManager, item: GalleryMediaItem) {
        glide
            .load(item.absolutePath)
            .placeholder(R.drawable.ic_launcher_background)
            .centerInside()
            .into(binding.ivImage)
    }
}