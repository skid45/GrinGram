package com.skid.gringram.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.databinding.GalleryItemBinding
import com.squareup.picasso.Picasso

class GalleryAdapter(private val actionListener: GalleryActionListener) :
    ListAdapter<Uri, GalleryAdapter.ViewHolder>(GalleryDiffCallback()) {

    class ViewHolder(val binding: GalleryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) = with(binding) {
            galleryItemImageView.tag = uri
            Picasso.get().load(uri).into(galleryItemImageView)
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GalleryItemBinding.inflate(view, parent, false)
        binding.galleryItemImageView.setOnClickListener {
            actionListener.onFullScreenPhoto(it.tag as Uri)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

interface GalleryActionListener {
    fun onFullScreenPhoto(uri: Uri)
}