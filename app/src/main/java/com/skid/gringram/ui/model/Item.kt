package com.skid.gringram.ui.model

interface Item {
    val type: ItemType
}

enum class ItemType {
    USER_TYPE,
    MESSAGE_TYPE,
    HEADER_TYPE,
}