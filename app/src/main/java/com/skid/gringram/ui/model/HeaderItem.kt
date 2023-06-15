package com.skid.gringram.ui.model

data class HeaderItem(
    val text: String,
) : Item {
    override val type: ItemType
        get() = ItemType.HEADER_TYPE
}
