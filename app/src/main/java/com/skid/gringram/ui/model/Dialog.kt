package com.skid.gringram.ui.model

data class Dialog(
    val companionUserUid: String? = "",
    val messages: Map<String, Message> = emptyMap(),
) {
    companion object {
        const val SOUND_ON = "sound on"
        const val SOUND_OFF = "sound off"
    }
}
