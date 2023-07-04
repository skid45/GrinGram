package com.skid.gringram.ui.adapter

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.appcompat.widget.ListPopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.skid.gringram.R
import com.skid.gringram.databinding.CustomAlertDialogLayoutBinding
import com.skid.gringram.databinding.MessageBinding
import com.skid.gringram.ui.model.Message
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.getTimeFromEpochMilliseconds

class ChatAdapter(
    private val companionUser: User?,
    private val actionListener: ChatActionListener,
) : MainAdapter<Message, ChatAdapter.ViewHolder>() {

    override var dataset: List<Message> = emptyList()
        set(newDataset) {
            val diffUtilCallback = MessageDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    override var currentUser: User? = null

    var messageKeys: List<String> = emptyList()

    private var listPopupWindow: ListPopupWindow? = null
    private var listPopupWindowIsShowing: Boolean = false

    class ViewHolder(
        val binding: MessageBinding,
        private val context: Context,
        private val actionListener: ChatActionListener,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message, currentUser: User?) = with(binding) {

            if (currentUser != null) {
                if (message.from == currentUser.uid) {
                    coreMessageLayout.apply {
                        gravity = Gravity.END
                        setPaddingRelative(50, 0, 0, 0)
                    }

                    viewedCheckmark.visibility = View.VISIBLE
                    if (message.viewed == true) {
                        viewedCheckmark.setImageResource(R.drawable.checkmark_done_icon)
                    } else {
                        viewedCheckmark.setImageResource(R.drawable.checkmark_icon_primary_color)
                    }
                } else {
                    coreMessageLayout.apply {
                        gravity = Gravity.START
                        setPaddingRelative(0, 0, 50, 0)
                    }

                    viewedCheckmark.visibility = View.GONE
                }

                if (message.text.isBlank()) messageText.visibility = View.GONE
                else {
                    messageText.text = message.text
                    messageText.visibility = View.VISIBLE
                }

                timestamp.text = message.timestamp.getTimeFromEpochMilliseconds()

                if (message.media.isEmpty()) {
                    messageMedia.visibility = View.GONE
                } else {
                    messageMediaRow1.removeAllViews()
                    messageMediaRow2.removeAllViews()
                    messageMediaRow3.removeAllViews()

                    val adapter = MessageMediaAdapter(
                        context = context,
                        mediaUris = message.media,
                        actionListener = object : MessageMediaActionListener {
                            override fun onFullscreenChatMedia(media: List<String>, position: Int) {
                                actionListener.onFullscreenChatMedia(media, position)
                            }
                        })
                    for (i in 0 until adapter.count) {
                        when {
                            i < 3 -> {
                                val view: View = adapter.getView(i, null, messageMediaRow1)
                                messageMediaRow1.addView(view)
                            }
                            i < 6 -> {
                                val view: View = adapter.getView(i, null, messageMediaRow2)
                                messageMediaRow2.addView(view)
                            }
                            else -> {
                                val view: View = adapter.getView(i, null, messageMediaRow3)
                                messageMediaRow3.addView(view)
                            }
                        }
                    }
                    messageMedia.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MessageBinding.inflate(view, parent, false)

        val holder = ViewHolder(binding, parent.context, actionListener)
        holder.binding.coreMessageLayout.setOnClickListener {
            if (listPopupWindowIsShowing) {
                listPopupWindow?.dismiss()
                listPopupWindowIsShowing = false
            } else {
                showPopupMenu(parent.context, holder)
            }
        }
        return holder
    }

    private fun showPopupMenu(context: Context, holder: ViewHolder) {
        if (listPopupWindow == null) {
            createListPopupWindow(context)
        }

        listPopupWindow?.anchorView = holder.binding.messageCardView

        val chatAdapterPosition = holder.adapterPosition
        if (chatAdapterPosition != RecyclerView.NO_POSITION) {
            val messageItem = dataset[chatAdapterPosition]

            if (messageItem.from == companionUser?.uid) listPopupWindow?.horizontalOffset = 150
            else listPopupWindow?.horizontalOffset = -200

            listPopupWindow?.setOnItemClickListener { adapterView, _, position, _ ->
                val popupItem = adapterView.getItemAtPosition(position) as Map<*, *>
                when (popupItem["text"] as String) {
                    context.getString(R.string.copy) -> {
                        val clipboardManager = context
                            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData = ClipData.newPlainText(null, messageItem.text)
                        clipboardManager.setPrimaryClip(clipData)
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

    private fun createListPopupWindow(context: Context) {
        val items = listOf(
            R.drawable.baseline_content_copy_24 to context.getString(R.string.copy),
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
        listPopupWindow = ListPopupWindow(context).apply {
            setAdapter(adapter)
            width = 350
            height = ListPopupWindow.WRAP_CONTENT
        }
    }

    private fun showDeleteMessageAlertDialog(context: Context, position: Int) {
        val customAlertDialogLayoutBinding =
            CustomAlertDialogLayoutBinding.inflate(LayoutInflater.from(context))
        val checkBoxText = "Also delete for ${companionUser?.username}"
        customAlertDialogLayoutBinding.customAlertDialogTitle.text =
            context.getString(R.string.delete_message)
        customAlertDialogLayoutBinding.customAlertDialogAlertMessage.text =
            context.getString(R.string.are_you_sure_you_want_to_delete_this_message)
        customAlertDialogLayoutBinding.customAlertDialogCheckbox.text = checkBoxText
        val alertDialogBuilder = MaterialAlertDialogBuilder(context)
        alertDialogBuilder
            .setView(customAlertDialogLayoutBinding.root)
            .setPositiveButton("Delete") { _, _ ->
                actionListener.deleteMessage(
                    messageKeys[position],
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


interface ChatActionListener {
    fun deleteMessage(messageKey: String, deleteBoth: Boolean)
    fun onFullscreenChatMedia(media: List<String>, position: Int)
}