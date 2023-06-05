package com.skid.gringram.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skid.gringram.R
import com.skid.gringram.databinding.NewMessageBottomSheetDialogBinding
import com.skid.gringram.ui.adapter.ContactListActionListener
import com.skid.gringram.ui.adapter.ContactListAdapter
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.contactsByQueryCollect
import com.skid.gringram.utils.userContactsCollect
import com.skid.gringram.utils.userStateCollect

class NewMessageBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: NewMessageBottomSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun getTheme() = R.style.BottomSheetDialogStyle

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val contactListAdapter by lazy {
        ContactListAdapter(object : ContactListActionListener {
            override fun onChatWithSelectedUser(companionUser: User) {
                val bundle = bundleOf("companionUser" to companionUser)
                findNavController().navigate(
                    R.id.action_chatListFragment_to_chatFragment, bundle
                )
                dismiss()
            }

            override fun addContact(uid: String) = userViewModel.addContact(uid)
            override fun removeContact(uid: String) = userViewModel.removeContact(uid)
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = NewMessageBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        recyclerViewInit()
        userStateCollect(userViewModel.currentUserState, contactListAdapter)
        userContactsCollect(
            userViewModel.currentUserContactList, contactListAdapter
        ) { it.username }
        setListeners()
    }

    override fun onPause() {
        super.onPause()
        binding.newMessageBottomSheetSearchEditText.setText("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recyclerViewInit() = with(binding) {
        newMessageBottomSheetContactListRecyclerView.layoutManager = LinearLayoutManager(context)
        newMessageBottomSheetContactListRecyclerView.adapter = contactListAdapter
    }

    private fun setListeners() {
        binding.apply {
            newMessageBottomSheetCancelButton.setOnClickListener {
                dismiss()
            }

            newMessageBottomSheetSearchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrBlank()) {
                        userContactsCollect(
                            userViewModel.currentUserContactList, contactListAdapter
                        ) { it.username }
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