package com.skid.gringram.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.skid.gringram.databinding.ContactListBinding
import com.skid.gringram.ui.adapter.ContactListAdapter
import kotlinx.coroutines.launch


class ContactListFragment : Fragment() {
    private var _binding: ContactListBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val contactListAdapter by lazy { ContactListAdapter() }

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
        userStateCollect()
        userContactsCollect()


        binding.searchViewEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrBlank()) userContactsCollect()
                else {
                    contactsByQueryCollect()
                    userViewModel.sendQueryToReceiveContacts(p0.toString())
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun recyclerViewInit() = with(binding) {
        contactListRecyclerView.layoutManager = LinearLayoutManager(context)
        contactListRecyclerView.adapter = contactListAdapter
    }


    private fun userStateCollect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserState.collect {
                    contactListAdapter.currentUser = it
                }
            }
        }
    }

    private fun userContactsCollect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.currentUserContactList.collect {
                    contactListAdapter.dataset = it
                }
            }
        }
    }

    private fun contactsByQueryCollect() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.contactsByQuery.collect {
                    contactListAdapter.dataset = it
                }
            }
        }
    }
}