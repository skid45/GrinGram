package com.skid.gringram.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.skid.gringram.ui.model.ChatListItem

class ChatListItemDiffUtilCallback(
    private val oldDataset: List<ChatListItem>,
    private val newDataset: List<ChatListItem>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDataset.size

    override fun getNewListSize(): Int = newDataset.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldDataset[oldItemPosition].companionUser?.uid == newDataset[newItemPosition].companionUser?.uid

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldDataset[oldItemPosition] == newDataset[newItemPosition]
}
