package com.wcjung.engstudy.domain.model

data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isEarned: Boolean
)
