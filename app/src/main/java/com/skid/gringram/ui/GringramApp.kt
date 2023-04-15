package com.skid.gringram.ui

import android.app.Application

class GringramApp : Application() {
    val firebaseAuthRepository by lazy { FirebaseAuthRepository() }
}