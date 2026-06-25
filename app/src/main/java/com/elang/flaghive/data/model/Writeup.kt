package com.elang.flaghive.data.model

data class Writeup(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val eventName: String = "",
    val challengeName: String = "",
    val difficulty: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
