package com.skid.gringram.ui.model

data class Dialog(
    val companionUserUid: String? = "",
    val messages: Map<String, Message> = emptyMap(),
)