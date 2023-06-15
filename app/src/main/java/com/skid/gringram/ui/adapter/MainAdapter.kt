package com.skid.gringram.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.ui.model.ChatListItem
import com.skid.gringram.ui.model.Message
import com.skid.gringram.ui.model.User

abstract class MainAdapter<T : Any, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    open var dataset: List<T> = emptyList()
    open var currentUser: User? = null

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    override fun getItemCount(): Int = dataset.size

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: VH, position: Int) {
        when (holder) {
            is ChatAdapter.ViewHolder -> holder.bind(dataset[position] as Message, currentUser)
            is ContactListAdapter.ViewHolder -> holder.bind(dataset[position] as User, currentUser)
            is ChatListAdapter.ViewHolder -> holder.bind(dataset[position] as ChatListItem)
            is SearchChatsDefaultAdapter.ViewHolder -> holder.bind(dataset[position] as User)
        }
    }
}