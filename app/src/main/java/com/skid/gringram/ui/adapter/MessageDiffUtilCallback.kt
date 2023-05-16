package com.skid.gringram.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.skid.gringram.ui.model.Message

class MessageDiffUtilCallback(
    private val oldDataset: List<Message>,
    private val newDataset: List<Message>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDataset.size

    override fun getNewListSize(): Int = newDataset.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldDataset[oldItemPosition].timestamp == newDataset[newItemPosition].timestamp

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldDataset[oldItemPosition] == newDataset[newItemPosition]
}