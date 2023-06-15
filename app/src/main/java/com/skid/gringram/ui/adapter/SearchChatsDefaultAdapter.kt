package com.skid.gringram.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.SearchChatsDefaultItemBinding
import com.skid.gringram.ui.model.User
import com.squareup.picasso.Picasso

class SearchChatsDefaultAdapter(
    private val actionListener: SearchChatsDefaultActionListener,
) : MainAdapter<User, SearchChatsDefaultAdapter.ViewHolder>(), OnClickListener {

    override var dataset: List<User> = emptyList()
        set(newDataset) {
            val diffUtilCallback = UserDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    class ViewHolder(private val binding: SearchChatsDefaultItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            searchChatsDefaultItemCoreLayout.tag = user

            searchChatsDefaultItemUsername.text = user.username
            if (user.photoUri != null) {
                Picasso.get().load(user.photoUri).fit().centerCrop()
                    .into(searchChatsDefaultItemImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = SearchChatsDefaultItemBinding.inflate(view, parent, false)

        binding.searchChatsDefaultItemCoreLayout.setOnClickListener(this)

        return ViewHolder(binding)
    }

    override fun onClick(p0: View) {
        val user = p0.tag as User
        when (p0.id) {
            R.id.search_chats_default_item_core_layout -> actionListener.onChatWithSelectedUser(user)
        }
    }
}

interface SearchChatsDefaultActionListener {
    fun onChatWithSelectedUser(companionUser: User)
}

