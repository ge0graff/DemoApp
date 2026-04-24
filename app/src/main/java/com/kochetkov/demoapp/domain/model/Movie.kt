package com.kochetkov.demoapp.domain.model

import java.io.Serializable

data class Movie(
    val id: Long,
    val title: String,
    val genre: String,
    val year: Int,
    val rating: Double,
    val imageUrl: String,
    val isLiked: Boolean = false
) : Serializable
