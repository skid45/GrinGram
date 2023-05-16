package com.skid.gringram.ui.model

data class Message(
    val text: String = "",
    val from: String = "",
    val timestamp: Long? = null,
)
