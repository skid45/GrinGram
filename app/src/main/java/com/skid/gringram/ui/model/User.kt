package com.skid.gringram.ui.model

import java.io.Serializable

data class User(
    val uid: String? = null,
    val username: String? = null,
    val photoUri: String? = null,
    val listOfContactsUri: MutableMap<String, String> = mutableMapOf(),
) : Serializable
