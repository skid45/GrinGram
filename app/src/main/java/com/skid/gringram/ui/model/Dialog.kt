package com.skid.gringram.ui.model

data class Dialog(
    val companionUserUid: String? = "",
    val mute: Boolean = SOUND_ON,
    val media: List<String> = emptyList(),
    val messages: Map<String, Message> = emptyMap(),
) {
    companion object {
        const val SOUND_ON = false
        const val SOUND_OFF = true
    }
}
