package com.lizpostudio.kgoptometrycrm.layer.domain.model

import java.io.Serializable

data class User(
    val uid: String,
    val email: String?,
    val isTrusted: Boolean,
    val isAdmin: Boolean
) : Serializable