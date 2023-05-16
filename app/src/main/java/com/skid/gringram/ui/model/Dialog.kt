package com.skid.gringram.ui.model

data class Dialog(
    val companionUserUid: String? = null,
    val messages: List<Message> = emptyList(),
)
