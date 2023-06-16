package com.skid.gringram.ui.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skid.gringram.R
import com.skid.gringram.databinding.ChatListItemBinding
import com.skid.gringram.databinding.CustomAlertDialogLayoutBinding
import com.skid.gringram.ui.model.ChatListItem
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.Constants
import com.skid.gringram.utils.getTimeOrDayOfWeekFromEpochMilliseconds
import com.squareup.picasso.Picasso

class ChatListAdapter(
    private val actionListener: ChatListActionListener,
) : MainAdapter<ChatListItem, ChatListAdapter.ViewHolder>() {

    override var dataset: List<ChatListItem> = emptyList()
        set(newDataset) {
            val diffUtilCallback = ChatListItemDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    var listPopupWindow: ListPopupWindow? = null
    var listPopupWindowIsShowing: Boolean = false

    class ViewHolder(val binding: ChatListItemBinding, private val context: Context) :
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
                    .getTimeOrDayOfWeekFromEpochMilliseconds()
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

            val chatNotificationsSharedPref = context.getSharedPreferences(
                Constants.SHARED_PREF_CHAT_NOTIFICATIONS_SOUND,
                Context.MODE_PRIVATE
            )
            val chatNotificationForUser =
                chatNotificationsSharedPref.getString(
                    chatListItem.companionUser?.uid,
                    Dialog.SOUND_ON
                )

            if (chatNotificationForUser == Dialog.SOUND_ON) {
                soundOffChatListItem.visibility = View.GONE
                newMessagesIndicatorCardView.backgroundTintList =
                    ColorStateList.valueOf(context.getColor(R.color.colorPrimary))
            } else {
                soundOffChatListItem.visibility = View.VISIBLE
                newMessagesIndicatorCardView.backgroundTintList =
                    ColorStateList.valueOf(context.getColor(R.color.colorOnSurfaceVariant))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChatListItemBinding.inflate(view, parent, false)
        val holder = ViewHolder(binding, parent.context)
        with(holder.binding) {
            chatListItemCoreLayout.setOnClickListener {
                if (listPopupWindowIsShowing) {
                    listPopupWindow?.dismiss()
                    listPopupWindowIsShowing = false
                } else {
                    actionListener.onChatWithSelectedUser(it.tag as User)
                }
            }

            chatListItemCoreLayout.setOnLongClickListener {
                showPopupMenu(parent.context, holder)
                true
            }
        }

        return holder
    }

    private fun showPopupMenu(context: Context, holder: ViewHolder) {
        val chatAdapterPosition = holder.adapterPosition
        if (chatAdapterPosition != RecyclerView.NO_POSITION) {
            val chatListItem = dataset[chatAdapterPosition]

            listPopupWindow =
                createListPopupWindow(context, chatListItem.companionUser?.uid.toString())

            listPopupWindow?.anchorView = holder.binding.chatListItemCoreLayout
            listPopupWindow?.horizontalOffset = 150

            listPopupWindow?.setOnItemClickListener { adapterView, _, position, _ ->
                val popupItem = adapterView.getItemAtPosition(position) as Map<*, *>
                val chatNotificationsSharedPref = context.getSharedPreferences(
                    Constants.SHARED_PREF_CHAT_NOTIFICATIONS_SOUND,
                    Context.MODE_PRIVATE
                )
                when (popupItem["text"] as String) {
                    context.getString(R.string.mute) -> {
                        with(chatNotificationsSharedPref.edit()) {
                            putString(chatListItem.companionUser?.uid, Dialog.SOUND_OFF)
                            apply()
                        }
                        notifyItemChanged(chatAdapterPosition)
                        listPopupWindow?.dismiss()
                        listPopupWindowIsShowing = false
                    }

                    context.getString(R.string.unmute) -> {
                        with(chatNotificationsSharedPref.edit()) {
                            putString(chatListItem.companionUser?.uid, Dialog.SOUND_ON)
                            apply()
                        }
                        notifyItemChanged(chatAdapterPosition)
                        listPopupWindow?.dismiss()
                        listPopupWindowIsShowing = false
                    }

                    context.getString(R.string.delete) -> {
                        showDeleteMessageAlertDialog(context, chatAdapterPosition)
                        listPopupWindow?.dismiss()
                        listPopupWindowIsShowing = false
                    }
                }
            }
        }
        listPopupWindow?.show()
        listPopupWindowIsShowing = true
    }

    private fun createListPopupWindow(context: Context, userUid: String): ListPopupWindow {
        val chatNotificationsSharedPref = context.getSharedPreferences(
            Constants.SHARED_PREF_CHAT_NOTIFICATIONS_SOUND,
            Context.MODE_PRIVATE
        )

        val chatNotificationForUser =
            chatNotificationsSharedPref.getString(userUid, Dialog.SOUND_ON)
        val notificationsStateIcon: Int
        val notificationsStateText: String
        if (chatNotificationForUser == Dialog.SOUND_ON) {
            notificationsStateIcon = R.drawable.baseline_notifications_off_24
            notificationsStateText = context.getString(R.string.mute)
        } else {
            notificationsStateIcon = R.drawable.baseline_notifications_24
            notificationsStateText = context.getString(R.string.unmute)
        }

        val items = listOf(
            notificationsStateIcon to notificationsStateText,
            R.drawable.baseline_delete_24 to context.getString(R.string.delete),
        )
        val data = mutableListOf<Map<String, Any>>()
        for (item in items) {
            data.add(mapOf("icon" to item.first, "text" to item.second))
        }

        val from = arrayOf("icon", "text")
        val to = intArrayOf(R.id.message_popup_icon, R.id.message_popup_text)

        val adapter =
            SimpleAdapter(context, data, R.layout.message_list_popup_window_item, from, to)

        return ListPopupWindow(context).apply {
            setAdapter(adapter)
            width = 350
            height = ListPopupWindow.WRAP_CONTENT
        }
    }

    private fun showDeleteMessageAlertDialog(context: Context, position: Int) {
        val customAlertDialogLayoutBinding =
            CustomAlertDialogLayoutBinding.inflate(LayoutInflater.from(context))
        val checkBoxText = "Also delete for ${dataset[position].companionUser?.username}"
        customAlertDialogLayoutBinding.customAlertDialogTitle.text =
            context.getString(R.string.delete_dialog)
        customAlertDialogLayoutBinding.customAlertDialogAlertMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_dialog)
        customAlertDialogLayoutBinding.customAlertDialogCheckbox.text = checkBoxText
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder
            .setView(customAlertDialogLayoutBinding.root)
            .setPositiveButton("Delete") { _, _ ->
                actionListener.deleteDialog(
                    dataset[position].companionUser?.uid.toString(),
                    customAlertDialogLayoutBinding.customAlertDialogCheckbox.isChecked
                )
            }
            .setNegativeButton("Cancel") { _, _ -> }
        val dialog = alertDialogBuilder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(context.getColor(R.color.colorError))
        }
        dialog.show()
    }
}

interface ChatListActionListener {
    fun onChatWithSelectedUser(companionUser: User)

    fun deleteDialog(companionUserUid: String, deleteBoth: Boolean)
}