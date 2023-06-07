package com.skid.gringram.ui.model

import java.io.Serializable

data class Device(
    val name: String? = null,
    val auth: Boolean? = null,
    val token: String? = null,
) : Serializable
