package com.kochetkov.demoapp.presentation.movies

import com.kochetkov.demoapp.domain.model.Movie

sealed interface MoviesIntent {
    data object LoadMovies : MoviesIntent
    data object Retry : MoviesIntent
}

data class MoviesState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val errorMessage: String? = null
)
