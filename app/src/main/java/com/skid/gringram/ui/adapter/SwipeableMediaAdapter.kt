package com.skid.gringram.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import com.squareup.picasso.Picasso

class SwipeMediaAdapter(
    private val media: List<String>,
    private val actionListener: SwipeMediaActionListener,
) : RecyclerView.Adapter<SwipeMediaAdapter.ViewHolder>() {
    class ViewHolder(val photoView: PhotoView) : RecyclerView.ViewHolder(photoView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val photoView = PhotoView(parent.context)
        photoView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setOnClickListener { actionListener.onPhotoClicking() }
        }


        return ViewHolder(photoView)
    }

    override fun getItemCount(): Int = media.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(media[position]).into(holder.photoView)
    }
}

interface SwipeMediaActionListener {
    fun onPhotoClicking()
}