package com.skid.gringram.ui.model

data class SearchedMessageItem(
    val messageUid: String,
    val user: User,
    val message: Message,
) : Item {
    override val type: ItemType
        get() = ItemType.MESSAGE_TYPE
}
