package com.skid.gringram.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.skid.gringram.ui.model.HeaderItem
import com.skid.gringram.ui.model.Item
import com.skid.gringram.ui.model.SearchedMessageItem
import com.skid.gringram.ui.model.User

class ItemDiffUtilCallback(
    private val oldDataset: List<Item>,
    private val newDataset: List<Item>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDataset.size

    override fun getNewListSize(): Int = newDataset.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldDataset[oldItemPosition]
        val newItem = newDataset[newItemPosition]
        return when {
            oldItem is User && newItem is User -> oldItem.uid == newItem.uid
            oldItem is SearchedMessageItem && newItem is SearchedMessageItem -> oldItem.messageUid == newItem.messageUid
            oldItem is HeaderItem && newItem is HeaderItem -> oldItem.text == newItem.text
            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldDataset[oldItemPosition]
        val newItem = newDataset[newItemPosition]
        return when {
            oldItem is User && newItem is User -> oldItem == newItem
            oldItem is SearchedMessageItem && newItem is SearchedMessageItem -> oldItem == newItem
            oldItem is HeaderItem && newItem is HeaderItem -> oldItem == newItem
            else -> false
        }
    }
}