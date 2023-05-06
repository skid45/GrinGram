package com.skid.gringram.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.ContactListItemBinding
import com.skid.gringram.ui.model.User
import com.squareup.picasso.Picasso

class ContactListAdapter(
    private val actionListener: ContactListActionListener,
) :
    RecyclerView.Adapter<ContactListAdapter.ViewHolder>(), OnClickListener {
    var dataset: List<User> = emptyList()
        set(newDataset) {
            val diffUtilCallback = UserDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    var currentUser: User? = null


    class ViewHolder(private val binding: ContactListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contactItem: User, currentUser: User?) = with(binding) {
            addContactButton.tag = contactItem
            removeContactButton.tag = contactItem
            contactItemLayout.tag = contactItem

            contactUsername.text = contactItem.username
            contactItem.photoUri.let {
                Picasso.get().load(it).fit().centerCrop().into(contactImage)
            }
            if (currentUser != null) {
                if (currentUser.listOfContactsUri.contains(contactItem.uid)) {
                    removeContactButton.visibility = View.VISIBLE
                    addContactButton.visibility = View.GONE
                } else {
                    addContactButton.visibility = View.VISIBLE
                    removeContactButton.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ContactListItemBinding.inflate(view, parent, false)

        binding.addContactButton.setOnClickListener(this)
        binding.removeContactButton.setOnClickListener(this)
        binding.contactItemLayout.setOnClickListener(this)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position], currentUser)
    }

    override fun onClick(p0: View) {
        val user = p0.tag as User
        when (p0.id) {
            R.id.add_contact_button -> user.uid?.let { actionListener.addContact(it) }
            R.id.remove_contact_button -> user.uid?.let { actionListener.removeContact(it) }
            R.id.contact_item_layout -> user.uid?.let { actionListener.onChatWithSelectedUser(it) }
        }
    }
}


interface ContactListActionListener {
    fun onChatWithSelectedUser(uid: String)

    fun addContact(uid: String)

    fun removeContact(uid: String)
}

