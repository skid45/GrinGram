package com.skid.gringram.ui.model

data class User(
    val uid: String? = null,
    val username: String? = null,
    val photoUri: String? = null,
    val contactList: MutableList<User> = mutableListOf(),
)
