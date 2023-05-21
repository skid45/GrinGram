package com.skid.gringram.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.MessageBinding
import com.skid.gringram.ui.model.Message
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.getTimeFromEpochMilliseconds

class ChatAdapter : MainAdapter<Message, ChatAdapter.ViewHolder>() {

    override var dataset: List<Message> = emptyList()
        set(newDataset) {
            val diffUtilCallback = MessageDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    override var currentUser: User? = null

    var messageKeys: List<String> = emptyList()

    class ViewHolder(private val binding: MessageBinding) : RecyclerView.ViewHolder(binding.root) {

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
                        viewedCheckmark.setImageResource(R.drawable.checkmark_icon)
                    }
                } else {
                    coreMessageLayout.apply {
                        gravity = Gravity.START
                        setPaddingRelative(0, 0, 50, 0)
                    }

                    viewedCheckmark.visibility = View.GONE
                }

                messageText.text = message.text
                timestamp.text = message.timestamp.getTimeFromEpochMilliseconds()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MessageBinding.inflate(view, parent, false)

        return ViewHolder(binding)
    }
}