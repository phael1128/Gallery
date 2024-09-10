package com.example.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.example.gallery.databinding.ItemGalleryBinding

class GalleryAdapter(
    private val mediaItemList: List<GalleryMediaItem>,
    private val glide: RequestManager
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return GalleryViewHolder(
            ItemGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as? GalleryViewHolder)?.onBind(glide, mediaItemList[position])
    }

    override fun getItemCount(): Int = mediaItemList.size

}