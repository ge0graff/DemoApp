package com.kochetkov.demoapp.presentation.movies

import com.kochetkov.demoapp.domain.model.Movie

sealed interface MoviesIntent {
    data object LoadMovies : MoviesIntent
    data object Retry : MoviesIntent
    data class ToggleLike(val movieId: Long) : MoviesIntent
}

sealed interface MoviesState {
    data object Loading : MoviesState

    data class Content(
        var movies: MutableList<Movie> = mutableListOf(),
        var errorMessage: String? = null
    ) : MoviesState
}
