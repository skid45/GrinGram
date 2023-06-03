package com.skid.gringram.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatListItemBinding
import com.skid.gringram.ui.model.ChatListItem
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.getTimeOrDayOfWeekFromEpochMilliseconds
import com.squareup.picasso.Picasso
import kotlinx.datetime.TimeZone

class ChatListAdapter(
    private val actionListener: ChatListActionListener,
) : MainAdapter<ChatListItem, ChatListAdapter.ViewHolder>(), OnClickListener {

    override var dataset: List<ChatListItem> = emptyList()
        set(newDataset) {
            val diffUtilCallback = ChatListItemDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    class ViewHolder(private val binding: ChatListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chatListItem: ChatListItem) = with(binding) {
            chatListItemCoreLayout.tag = chatListItem.companionUser

            Picasso.get().load(chatListItem.companionUser?.photoUri).fit().centerCrop()
                .into(imageChatListItem)
            textNameChatListItem.text = chatListItem.companionUser?.username ?: ""

            if (chatListItem.companionUser?.online == true) {
                onlineIndicatorChatListItem.visibility = View.VISIBLE
            } else onlineIndicatorChatListItem.visibility = View.GONE

            val lastMessage = chatListItem.dialog?.messages?.values?.last()
            if (lastMessage != null) {
                textMessageChatListItem.text = lastMessage.text
                timeChatListItem.text = lastMessage
                    .timestamp
                    .getTimeOrDayOfWeekFromEpochMilliseconds(TimeZone.currentSystemDefault())
                if (lastMessage.from != chatListItem.companionUser?.uid) {
                    if (lastMessage.viewed == false) {
                        viewedCheckmarkChatListItem.setImageResource(R.drawable.checkmark_icon_primary_color)
                    } else {
                        viewedCheckmarkChatListItem.setImageResource(R.drawable.checkmark_done_icon)
                    }
                    viewedCheckmarkChatListItem.visibility = View.VISIBLE
                } else viewedCheckmarkChatListItem.visibility = View.GONE
            }

            val countOfNewMessages =
                chatListItem
                    .dialog
                    ?.messages
                    ?.values
                    ?.count { it.from == chatListItem.dialog.companionUserUid && it.viewed == false }
            if (countOfNewMessages == 0) {
                newMessagesIndicator.visibility = View.GONE
            } else {
                newMessagesIndicator.text = countOfNewMessages.toString()
                newMessagesIndicator.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChatListItemBinding.inflate(view, parent, false)

        binding.chatListItemCoreLayout.setOnClickListener(this)

        return ViewHolder(binding)
    }

    override fun onClick(p0: View) {
        val user = p0.tag as User
        when (p0.id) {
            R.id.chat_list_item_core_layout -> actionListener.onChatWithSelectedUser(user)
        }
    }
}

interface ChatListActionListener {
    fun onChatWithSelectedUser(companionUser: User)
}