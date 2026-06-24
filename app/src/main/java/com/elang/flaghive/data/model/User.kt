package com.elang.flaghive.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.USER,
    val createdAt: Long = System.currentTimeMillis()
)
