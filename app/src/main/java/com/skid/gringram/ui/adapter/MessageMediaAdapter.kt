package com.skid.gringram.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.skid.gringram.R
import com.squareup.picasso.Picasso

class MessageMediaAdapter(
    private val context: Context,
    private val mediaUris: List<String>,
    private val actionListener: MessageMediaActionListener,
) : BaseAdapter() {

    private class ViewHolder(val imageView: ImageView)

    override fun getCount(): Int = mediaUris.size

    override fun getItem(position: Int): Any = mediaUris[position]

    override fun getItemId(position: Int): Long = 0

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.message_media_item, parent, false)
            viewHolder = ViewHolder(view as ImageView)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = convertView.tag as ViewHolder
        }

        Picasso.get().load(mediaUris[position]).into(viewHolder.imageView)

        view.setOnClickListener {
            actionListener.onFullscreenChatMedia(mediaUris, position)
        }

        return view
    }
}

interface MessageMediaActionListener {
    fun onFullscreenChatMedia(media: List<String>, position: Int)
}