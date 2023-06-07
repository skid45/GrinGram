package com.skid.gringram.ui.repositories

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.skid.gringram.ui.model.User
import com.skid.gringram.utils.getDeviceName
import kotlinx.coroutines.flow.MutableStateFlow

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()


    fun signUp(username: String, email: String, password: String, signUpState: MutableStateFlow<Boolean?>) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    addUser(User(auth.uid, username), signUpState)
                } else {
                    signUpState.value = false
                    Log.w("Auth", "createUserWithEmail:failure", it.exception)
                }
            }
    }

    private fun addUser(user: User, signUpState: MutableStateFlow<Boolean?>) {
        database.getReference("users").child(auth.uid.toString()).setValue(user)
            .addOnCompleteListener {
                signUpState.value = it.isSuccessful
                sendUserDeviceAuth(isAuthenticated = true)
            }
    }


    fun signIn(email: String, password: String, signInState: MutableStateFlow<Boolean?>) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    signInState.value = true
                    sendUserDeviceAuth(isAuthenticated = true)
                } else {
                    signInState.value = false
                    Log.w("Auth", "signInWithEmail:failure", it.exception)
                }
            }
    }

    fun signOut() {
        sendUserDeviceAuth(isAuthenticated = false)
        auth.signOut()
    }

    private fun sendUserDeviceAuth(isAuthenticated: Boolean) {
        val ref = "users/${auth.uid}/devices/${getDeviceName()}/auth"
        database.reference.child(ref).setValue(isAuthenticated)
    }
}