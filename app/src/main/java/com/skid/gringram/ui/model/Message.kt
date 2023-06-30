package com.skid.gringram.ui.model

data class Message(
    val text: String = "",
    val from: String = "",
    val media: List<String> = emptyList(),
    val timestamp: Long? = null,
    val viewed: Boolean? = null,
)
