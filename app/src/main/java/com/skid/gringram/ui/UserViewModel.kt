package com.skid.gringram.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.skid.gringram.ui.model.Dialog
import com.skid.gringram.ui.model.User
import com.skid.gringram.ui.repositories.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

class UserViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    val currentUserState: StateFlow<User?> = userRepository.currentUserState.asStateFlow()
    val currentUserContactList: StateFlow<Set<User>> =
        userRepository.currentUserContactList.asStateFlow()
    val currentUserDialogs: StateFlow<List<Dialog>> =
        userRepository.currentUserDialogs.asStateFlow()
    val contactsByQuery: StateFlow<List<User>> = userRepository.contactsByQuery.asStateFlow()

    init {
        userRepository.addCurrentUserValueEventListener()
    }

    fun changeUserPhoto(uri: Uri) {
        userRepository.changeUserPhoto(uri)
    }

    fun sendQueryToReceiveContacts(queryString: String) {
        userRepository.sendQueryToReceiveContacts(queryString)
    }

    fun addContact(uid: String) {
        userRepository.addContact(uid)
    }

    fun removeContact(uid: String) {
        userRepository.removeContact(uid)
    }

    fun sendMessage(text: String, recipientUserUid: String) {
        userRepository.sendMessage(text, recipientUserUid)
    }

    override fun onCleared() {
        super.onCleared()
        userRepository.removeCurrentUserValueEventListener()
    }
}

@Suppress("UNCHECKED_CAST")
class UserViewModelFactory(private val application: GringramApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(
                application.userRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}