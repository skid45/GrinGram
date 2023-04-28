package com.skid.gringram.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.skid.gringram.ui.model.User

class UserDiffUtilCallback(
    private val oldDataset: List<User>,
    private val newDataset: List<User>,
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldDataset.size

    override fun getNewListSize(): Int = newDataset.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition].uid == newDataset[newItemPosition].uid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDataset[oldItemPosition] == newDataset[newItemPosition]
    }
}