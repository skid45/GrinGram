package com.skid.gringram.ui

import android.util.Log
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val firebaseAuthRepository: FirebaseAuthRepository) : ViewModel() {

    private val _signInState = MutableStateFlow(false)
    val signInState = _signInState.asStateFlow()
    private val _signUpState = MutableStateFlow(false)
    val signUpState = _signUpState.asStateFlow()


    fun checkUserSignIn(navController: NavController, @IdRes resIdRes: Int) {
        firebaseAuthRepository.checkUserSignIn(navController, resIdRes)
    }

    suspend fun signUp(email: String, password: String) {
        firebaseAuthRepository.signUp(email, password, _signUpState)
        Log.d("Auth", signUpState.value.toString())
    }

    suspend fun signIn(email: String, password: String) {
        firebaseAuthRepository.signIn(email, password, _signInState)
    }

    fun signOut() {
        firebaseAuthRepository.signOut()
        _signInState.value = false
        _signUpState.value = false
    }
}

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory(private val application: GringramApp) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(application.firebaseAuthRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}