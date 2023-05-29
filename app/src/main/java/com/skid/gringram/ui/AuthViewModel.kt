package com.skid.gringram.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.skid.gringram.ui.repositories.AuthRepository
import com.skid.gringram.ui.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _signInState: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val signInState = _signInState.asStateFlow()
    private val _signUpState: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    val signUpState = _signUpState.asStateFlow()


    fun signUp(username: String, email: String, password: String) {
        authRepository.signUp(username, email, password, _signUpState)
        viewModelScope.launch {
            signUpState.collect {
                if (it == true) userRepository.addCurrentUserValueEventListener()
            }
        }
    }

    fun signIn(email: String, password: String) {
        authRepository.signIn(email, password, _signInState)
        viewModelScope.launch {
            signInState.collect {
                if (it == true) userRepository.addCurrentUserValueEventListener()
            }
        }
    }

    fun signOut() {
        userRepository.changeUserOnlineStatus(isOnline = false)
        userRepository.removeCurrentUserValueEventListener()
        authRepository.signOut()
        _signInState.value = null
        _signUpState.value = null
    }

}

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(private val application: GringramApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(
                application.userRepository,
                application.authRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
