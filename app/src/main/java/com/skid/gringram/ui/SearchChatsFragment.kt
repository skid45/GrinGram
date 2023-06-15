package com.skid.gringram.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.transition.TransitionInflater
import com.skid.gringram.R
import com.skid.gringram.databinding.SearchChatsBinding
import com.skid.gringram.ui.adapter.SearchChatsActionListener
import com.skid.gringram.ui.adapter.SearchChatsAdapter
import com.skid.gringram.ui.adapter.SearchChatsDefaultActionListener
import com.skid.gringram.ui.adapter.SearchChatsDefaultAdapter
import com.skid.gringram.ui.model.SearchedMessageItem
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.Constants.COMPANION_USER
import com.skid.gringram.utils.Constants.MESSAGE_KEY_FOR_SCROLL
import kotlinx.coroutines.launch

class SearchChatsFragment : Fragment() {
    private var _binding: SearchChatsBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by activityViewModels {
        UserViewModelFactory(activity?.application as GringramApp)
    }

    private val searchChatsDefaultAdapter by lazy {
        SearchChatsDefaultAdapter(object : SearchChatsDefaultActionListener {
            override fun onChatWithSelectedUser(companionUser: User) {
                val bundle = bundleOf(COMPANION_USER to companionUser)
                findNavController().navigate(
                    R.id.action_searchChatsFragment_to_chatFragment,
                    bundle
                )
            }
        })
    }

    private val searchChatsAdapter by lazy {
        SearchChatsAdapter(object : SearchChatsActionListener {
            override fun onChatWithChatAndContactsItem(user: User) {
                val bundle = bundleOf(COMPANION_USER to user)
                findNavController().navigate(
                    R.id.action_searchChatsFragment_to_chatFragment,
                    bundle
                )
            }

            override fun onChatWithMessage(searchedMessageItem: SearchedMessageItem) {
                val bundle = bundleOf(
                    COMPANION_USER to searchedMessageItem.user,
                    MESSAGE_KEY_FOR_SCROLL to searchedMessageItem.messageUid
                )
                findNavController().navigate(
                    R.id.action_searchChatsFragment_to_chatFragment,
                    bundle
                )
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_element_transition)
        sharedElementReturnTransition =
            TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.shared_element_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = SearchChatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFocusOnSearchField()
        recyclerViewInit()
        setListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.chatListItems.collect { chatList ->
                    searchChatsDefaultAdapter.dataset = chatList
                        .sortedBy { it.dialog?.messages?.size }
                        .reversed()
                        .map { it.companionUser!! }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.searchedDatasetForAdapterFlow.collect {
                    searchChatsAdapter.dataset = it
                }
            }
        }
    }

    private fun setFocusOnSearchField() = with(binding) {
        searchChatsEditText.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchChatsEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun recyclerViewInit() {
        binding.apply {
            searchChatsDefaultRecyclerView.layoutManager =
                LinearLayoutManager(context, HORIZONTAL, false)
            searchChatsDefaultRecyclerView.adapter = searchChatsDefaultAdapter

            searchChatsInSearchRecyclerView.layoutManager = LinearLayoutManager(context)
            searchChatsInSearchRecyclerView.adapter = searchChatsAdapter
        }
    }

    private fun setListeners() {
        binding.apply {
            searchChatsCancelButton.setOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            searchChatsEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (p0.isNullOrBlank()) {
                        searchChatsDefaultRecyclerView.visibility = View.VISIBLE
                        searchChatsInSearchRecyclerView.visibility = View.GONE
                    } else {
                        lifecycleScope.launch {
                            userViewModel.searchChats(p0.toString())
                        }

                        searchChatsDefaultRecyclerView.visibility = View.GONE
                        searchChatsInSearchRecyclerView.visibility = View.VISIBLE

                    }
                }

                override fun afterTextChanged(p0: Editable?) = Unit

            })
        }
    }
}