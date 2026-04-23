package com.kochetkov.demoapp.domain.repository

import com.kochetkov.demoapp.domain.model.Movie

interface MovieRepository {
    suspend fun getMovies(): List<Movie>
}
