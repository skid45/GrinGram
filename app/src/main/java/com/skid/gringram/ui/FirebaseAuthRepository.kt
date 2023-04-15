package com.skid.gringram.ui

import android.util.Log
import androidx.annotation.IdRes
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository {

    private val auth by lazy { FirebaseAuth.getInstance() }

    fun checkUserSignIn(navController: NavController, @IdRes resIdRes: Int) {
        if (auth.currentUser == null) {
            navController
                .navigate(resIdRes)
        }
    }

    suspend fun signUp(email: String, password: String, signUpState: MutableStateFlow<Boolean>) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signUpState.value = true
                    Log.d("Auth", "createUserWithEmail:success signUpState${signUpState.value}")
                } else {
                    signUpState.value = false
                    Log.w("Auth", "createUserWithEmail:failure", it.exception)
                }
            }.await()
    }

    suspend fun signIn(email: String, password: String, signInState: MutableStateFlow<Boolean>) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signInState.value = true
                    Log.d("Auth", "signInWithEmail:success")
                } else {
                    signInState.value = false
                    Log.w("Auth", "signInWithEmail:failure", it.exception)
                }
            }.await()
    }

    fun signOut() {
        auth.signOut()
    }
}