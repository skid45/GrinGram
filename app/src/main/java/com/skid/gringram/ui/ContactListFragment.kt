package com.skid.gringram.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.skid.gringram.R
import com.skid.gringram.databinding.ContactListBinding
import com.skid.gringram.ui.adapter.ContactListActionListener
import com.skid.gringram.ui.adapter.ContactListAdapter
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.contactsByQueryCollect
import com.skid.gringram.utils.userContactsCollect
import com.skid.gringram.utils.userStateCollect


class ContactListFragment : Fragment() {
    private var _binding: ContactListBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val contactListAdapter by lazy {
        ContactListAdapter(object : ContactListActionListener {
            override fun onChatWithSelectedUser(companionUser: User) {
                val bundle = bundleOf("companionUser" to companionUser)
                findNavController().navigate(
                    R.id.action_contactListFragment_to_chatFragment, bundle
                )
            }

            override fun addContact(uid: String) = userViewModel.addContact(uid)
            override fun removeContact(uid: String) = userViewModel.removeContact(uid)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = ContactListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewInit()
        userStateCollect(userViewModel.currentUserState, contactListAdapter)
        userContactsCollect(
            userViewModel.currentUserContactList, contactListAdapter
        ) { it.onlineTimestamp }
        setListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        binding.searchViewEditText.setText("")
    }

    private fun recyclerViewInit() = with(binding) {
        contactListRecyclerView.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        contactListRecyclerView.adapter = contactListAdapter
    }

    private fun setListeners() {
        binding.apply {
            searchViewEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrBlank()) {
                        userContactsCollect(
                            userViewModel.currentUserContactList, contactListAdapter
                        ) { it.onlineTimestamp }
                    } else {
                        contactsByQueryCollect(userViewModel.contactsByQuery, contactListAdapter) {
                            it.username?.startsWith(p0.toString(), true)!!
                        }
                        userViewModel.sendQueryToReceiveContacts(p0.toString())
                    }
                }

                override fun afterTextChanged(p0: Editable?) = Unit
            })
        }
    }
}