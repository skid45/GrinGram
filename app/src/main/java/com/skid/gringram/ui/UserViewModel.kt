package com.skid.gringram.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skid.gringram.ui.model.*
import com.skid.gringram.ui.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class UserViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    val currentUserState: StateFlow<User?> = userRepository.currentUserState.asStateFlow()
    val currentUserContactList: StateFlow<Set<User>> =
        userRepository.currentUserContactList.asStateFlow()
    val currentUserDialogs: StateFlow<List<Dialog>> =
        userRepository.currentUserDialogs.asStateFlow()
    private val usersForDialogs: StateFlow<List<User>> =
        userRepository.usersForDialogs.asStateFlow()
    val companionUserForChatState: StateFlow<User?> =
        userRepository.companionUserForChatState.asStateFlow()
    private val _chatListItems: MutableStateFlow<List<ChatListItem>> = MutableStateFlow(emptyList())
    val chatListItems: StateFlow<List<ChatListItem>> = _chatListItems.asStateFlow()
    val contactsByQuery: StateFlow<List<User>> = userRepository.contactsByQuery.asStateFlow()
    private val _usernameIsValid: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val usernameIsValid: StateFlow<Boolean> = _usernameIsValid.asStateFlow()
    private val _searchedDatasetForAdapterFlow: MutableStateFlow<List<Item>> =
        MutableStateFlow(emptyList())
    val searchedDatasetForAdapterFlow: StateFlow<List<Item>> =
        _searchedDatasetForAdapterFlow.asStateFlow()

    init {
        userRepository.addCurrentUserValueEventListener()
    }

    fun getChatListItem() {
        viewModelScope.launch {
            usersForDialogs.collect {
                if (it.size == currentUserDialogs.value.size) {
                    _chatListItems.value = it.map { user ->
                        ChatListItem(
                            user,
                            currentUserDialogs.value
                                .first { dialog -> dialog.companionUserUid == user.uid })
                    }
                }
            }
        }
    }

    fun changeUserPhoto(uri: Uri) {
        userRepository.changeUserPhoto(uri)
    }

    fun changeUserOnlineStatus(isOnline: Boolean) {
        userRepository.changeUserOnlineStatus(isOnline)
    }

    fun sendQueryToReceiveContacts(queryString: String) {
        userRepository.sendQueryToReceiveContacts(queryString)
    }

    fun addCompanionUserForChatStateValueEventListener(companionUserUid: String) {
        userRepository.addCompanionUserForChatStateValueEventListener(companionUserUid)
    }

    fun removeCompanionUserForChatStateValueEventListener(companionUserUid: String) {
        userRepository.removeCompanionUserForChatStateValueEventListener(companionUserUid)
    }

    fun addContact(uid: String) {
        userRepository.addContact(uid)
    }

    fun removeContact(uid: String) {
        userRepository.removeContact(uid)
    }

    fun sendMessage(text: String, recipientUserUid: String, context: Context) {
        userRepository.sendMessage(text, recipientUserUid, context)
    }

    fun updateMessageStatus(messageKey: String?, recipientUserUid: String) {
        userRepository.updateMessageStatus(messageKey, recipientUserUid)
    }

    override fun onCleared() {
        super.onCleared()
        _chatListItems.value = emptyList()
        _searchedDatasetForAdapterFlow.value = emptyList()
        userRepository.removeCurrentUserValueEventListener()
    }

    fun deleteMessage(messageKey: String, deleteBoth: Boolean, recipientUserUid: String) {
        userRepository.deleteMessage(messageKey, deleteBoth, recipientUserUid)
    }

    fun validateUsername(username: String) {
        viewModelScope.launch {
            _usernameIsValid.value = userRepository.validateUsername(username)
        }
    }

    fun changeUsername(username: String) {
        userRepository.changeUsername(username)
    }

    fun sendTokenToServer(token: String) {
        userRepository.sendTokenToServer(token)
    }

    fun searchChats(query: String) {
        viewModelScope.launch {
            _searchedDatasetForAdapterFlow.value = emptyList()
            withContext(Dispatchers.Default) {
                searchChatsChatsAndContacts(query)
                searchChatsMessages(query)
            }
        }
    }

    private fun searchChatsChatsAndContacts(query: String) {
        viewModelScope.launch {
            _searchedDatasetForAdapterFlow.value = emptyList()
            val searchedUsers = withContext(Dispatchers.Default) {
                chatListItems.value.asSequence().filter {
                    it.companionUser?.username?.startsWith(query, true) == true
                }.mapNotNull { it.companionUser }.toList()
            }
            if (searchedUsers.isNotEmpty()) {
                _searchedDatasetForAdapterFlow.value = mutableListOf<Item>().apply {
                    add(HeaderItem("Chats and contacts"))
                    addAll(searchedUsers)
                }.toList()
            }
        }
    }

    private fun searchChatsMessages(query: String) {
        viewModelScope.launch {
            val searchedMessages = withContext(Dispatchers.Default) {
                chatListItems.value.asSequence().flatMap { chatListItem ->
                    chatListItem.dialog?.messages?.asSequence()?.filter { (_, v) ->
                        v.text.contains(query, true)
                    }?.mapNotNull { (k, v) ->
                        chatListItem.companionUser?.let { user -> SearchedMessageItem(k, user, v) }
                    }.orEmpty()
                }.toList().reversed()
            }

            if (searchedMessages.isNotEmpty()) {
                _searchedDatasetForAdapterFlow.value += mutableListOf<Item>().apply {
                    add(HeaderItem("Messages"))
                    addAll(searchedMessages)
                }.toList()
            }
        }
    }

}

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(private val application: GringramApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(
                application.userRepository,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}