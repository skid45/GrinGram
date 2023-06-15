package com.skid.gringram.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatListItemBinding
import com.skid.gringram.databinding.SearchChatsChatAndContactsItemBinding
import com.skid.gringram.databinding.SearchChatsHeaderItemBinding
import com.skid.gringram.ui.model.*
import com.skid.gringram.utils.getTimeOrDayOfWeekFromEpochMilliseconds
import com.squareup.picasso.Picasso

class SearchChatsAdapter(
    private val actionListener: SearchChatsActionListener,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), OnClickListener {

    var dataset: List<Item> = emptyList()
        set(newDataset) {
            val diffUtilCallback = ItemDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    override fun getItemViewType(position: Int): Int {
        return when (dataset[position].type) {
            ItemType.USER_TYPE -> 1
            ItemType.MESSAGE_TYPE -> 2
            else -> 3 //ItemType.HEADER_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                val view = LayoutInflater.from(parent.context)
                val binding = SearchChatsChatAndContactsItemBinding.inflate(view, parent, false)
                binding.searchChatsChatAndContactsItemCoreLayout.setOnClickListener(this)
                UserViewHolder(binding)
            }

            2 -> {
                val view = LayoutInflater.from(parent.context)
                val binding = ChatListItemBinding.inflate(view, parent, false)
                binding.chatListItemCoreLayout.setOnClickListener(this)
                MessageViewHolder(binding)
            }

            else -> {
                val view = LayoutInflater.from(parent.context)
                val binding = SearchChatsHeaderItemBinding.inflate(view, parent, false)
                HeaderViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataset[position]
        when (holder) {
            is UserViewHolder -> holder.bind(item as User)
            is MessageViewHolder -> holder.bind(item as SearchedMessageItem)
            is HeaderViewHolder -> holder.bind(item as HeaderItem)
        }
    }


    class UserViewHolder(private val binding: SearchChatsChatAndContactsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) = with(binding) {
            searchChatsChatAndContactsItemCoreLayout.tag = user

            searchChatsChatAndContactsUsername.text = user.username
            Picasso.get().load(user.photoUri).fit().centerCrop()
                .into(searchChatsChatAndContactsImage)
        }
    }

    class MessageViewHolder(private val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(message: SearchedMessageItem) = with(binding) {
            chatListItemCoreLayout.tag = message

            if (message.message.from != message.user.uid) {
                if (message.message.viewed == true) {
                    viewedCheckmarkChatListItem.setImageResource(R.drawable.checkmark_done_icon)
                } else {
                    viewedCheckmarkChatListItem.setImageResource(R.drawable.checkmark_icon_primary_color)
                }
                viewedCheckmarkChatListItem.visibility = View.VISIBLE

            } else viewedCheckmarkChatListItem.visibility = View.GONE

            textNameChatListItem.text = message.user.username
            textMessageChatListItem.text = message.message.text
            Picasso.get().load(message.user.photoUri).fit().centerCrop().into(imageChatListItem)
            timeChatListItem.text =
                message.message.timestamp.getTimeOrDayOfWeekFromEpochMilliseconds()

        }
    }

    class HeaderViewHolder(private val binding: SearchChatsHeaderItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: HeaderItem) = with(binding) {
            searchChatsHeaderTextView.text = header.text
        }
    }

    override fun onClick(p0: View) {
        val item = p0.tag
        when (p0.id) {
            R.id.search_chats_chat_and_contacts_item_core_layout ->
                actionListener.onChatWithChatAndContactsItem(item as User)
            R.id.chat_list_item_core_layout ->
                actionListener.onChatWithMessage(item as SearchedMessageItem)
        }
    }
}

interface SearchChatsActionListener {
    fun onChatWithChatAndContactsItem(user: User)
    fun onChatWithMessage(searchedMessageItem: SearchedMessageItem)
}