package com.kochetkov.demoapp.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kochetkov.demoapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MoviesState())
    val state: StateFlow<MoviesState> = _state.asStateFlow()

    fun accept(intent: MoviesIntent) {
        when (intent) {
            MoviesIntent.LoadMovies -> loadMovies()
            MoviesIntent.Retry -> loadMovies()
            is MoviesIntent.ToggleLike -> toggleLike(intent.movieId)
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getMovies() }
                .onSuccess { movies ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            movies = movies,
                            errorMessage = null
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            movies = emptyList(),
                            errorMessage = "Failed to load movies. Tap to retry."
                        )
                    }
                }
        }
    }

    private fun toggleLike(movieId: Long) {
        _state.update { state ->
            state.copy(
                movies = state.movies.map { movie ->
                    if (movie.id == movieId) {
                        movie.copy(isLiked = !movie.isLiked)
                    } else {
                        movie
                    }
                }
            )
        }
    }

    class Factory(
        private val repository: MovieRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(MoviesViewModel::class.java)) {
                "Unknown ViewModel class: ${modelClass.name}"
            }
            return MoviesViewModel(repository) as T
        }
    }
}
