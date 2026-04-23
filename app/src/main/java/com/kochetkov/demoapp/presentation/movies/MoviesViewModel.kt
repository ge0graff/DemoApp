package com.kochetkov.demoapp.presentation.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kochetkov.demoapp.domain.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoviesViewModel(
    private val repository: MovieRepository
) : ViewModel() {

    private val _state = MutableStateFlow<MoviesState>(MoviesState.Loading)
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
            _state.value = MoviesState.Loading
            runCatching { repository.getMovies() }
                .onSuccess { movies ->
                    _state.value = MoviesState.Content(
                        movies = movies.toMutableList(),
                        errorMessage = null
                    )
                }
                .onFailure {
                    _state.value = MoviesState.Content(
                        movies = mutableListOf(),
                        errorMessage = "Failed to load movies. Tap to retry."
                    )
                }
        }
    }

    private fun toggleLike(movieId: Long) {
        val currentState = _state.value as? MoviesState.Content ?: return
        val movieIndex = currentState.movies.indexOfFirst { it.id == movieId }
        if (movieIndex == -1) return

        val movie = currentState.movies[movieIndex]
        currentState.movies[movieIndex] = movie.copy(isLiked = !movie.isLiked)
        _state.value = currentState
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
