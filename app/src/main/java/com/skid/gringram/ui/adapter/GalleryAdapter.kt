package com.skid.gringram.ui.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.GalleryItemBinding
import com.squareup.picasso.Picasso
import kotlinx.coroutines.flow.MutableStateFlow

class GalleryAdapter(
    private val canAttach: Boolean,
    private val actionListener: GalleryActionListener,
) : ListAdapter<Uri, GalleryAdapter.ViewHolder>(GalleryDiffCallback()) {

    class ViewHolder(
        val binding: GalleryItemBinding,
        private val canAttach: Boolean,
        private val actionListener: GalleryActionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) = with(binding) {
            galleryItemImageView.tag = uri
            Picasso.get().load(uri).into(galleryItemImageView)

            if (canAttach) {
                galleryItemSelectImageTextView.visibility = View.VISIBLE
                val indexOfUri =
                    actionListener.getListOfSelected().values.indexOfFirst { it == uri } + 1
                if (indexOfUri > 0) {
                    galleryItemSelectImageTextView.apply {
                        background = AppCompatResources.getDrawable(
                            context, R.drawable.circle_border_filled_background
                        )
                        text = indexOfUri.toString()
                    }
                } else {
                    galleryItemSelectImageTextView.apply {
                        text = ""
                        background = AppCompatResources.getDrawable(
                            context, R.drawable.circle_border_background
                        )
                    }
                }
            } else galleryItemSelectImageTextView.visibility = View.GONE
        }
    }

    class GalleryDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GalleryItemBinding.inflate(view, parent, false)

        val holder = ViewHolder(binding, canAttach, actionListener)

        holder.binding.apply {

            galleryItemImageView.setOnClickListener {
                val position = holder.adapterPosition
                actionListener.onFullScreenPhoto(it.tag as Uri, position)
            }

            if (canAttach) {
                galleryItemSelectImageTextView.setOnClickListener {
                    val position = holder.adapterPosition
                    if (!actionListener.getListOfSelected().containsKey(position)) {
                        actionListener.addSelectedItem(galleryItemImageView.tag as Uri, position)
                    } else {
                        actionListener.removeSelectedItem(position)
                        notifyItemChanged(position)
                    }
                }
            }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateSelected(positions: Set<Int>) {
        positions.forEach { notifyItemChanged(it) }
    }
}

interface GalleryActionListener {
    fun onFullScreenPhoto(uri: Uri, position: Int)
    fun addSelectedItem(uri: Uri, position: Int)
    fun removeSelectedItem(position: Int)
    fun getListOfSelected(): Map<Int, Uri>
    fun getStateOfSelected(): MutableStateFlow<LinkedHashMap<Int, Uri>>
}