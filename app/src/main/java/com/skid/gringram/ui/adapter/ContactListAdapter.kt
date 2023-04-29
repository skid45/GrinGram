package com.skid.gringram.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.skid.gringram.R
import com.skid.gringram.databinding.ContactListItemBinding
import com.skid.gringram.ui.model.User
import com.squareup.picasso.Picasso

class ContactListAdapter :
    RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {
    var dataset: List<User> = emptyList()
        set(newDataset) {
            val diffUtilCallback = UserDiffUtilCallback(field, newDataset)
            val diffResult = DiffUtil.calculateDiff(diffUtilCallback)
            field = newDataset
            diffResult.dispatchUpdatesTo(this)
        }

    var currentUser: User? = null


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ContactListItemBinding.bind(view)

        fun bind(contactItem: User, currentUser: User?) = with(binding) {
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
        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataset.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataset[position], currentUser)
    }
}