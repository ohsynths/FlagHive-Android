package com.elang.flaghive.data.model

data class Bookmark(
    val id: String = "",
    val userId: String = "",
    val writeupId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
