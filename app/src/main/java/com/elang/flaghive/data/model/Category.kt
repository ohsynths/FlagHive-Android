package com.elang.flaghive.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
