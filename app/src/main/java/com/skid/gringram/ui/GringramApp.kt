package com.skid.gringram.ui

import android.app.Application
import com.skid.gringram.ui.repositories.AuthRepository
import com.skid.gringram.ui.repositories.UserRepository

class GringramApp : Application() {
    val userRepository by lazy { UserRepository() }
    val authRepository by lazy { AuthRepository() }
}