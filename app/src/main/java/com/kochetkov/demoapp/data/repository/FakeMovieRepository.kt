package com.kochetkov.demoapp.data.repository

import com.kochetkov.demoapp.domain.model.Movie
import com.kochetkov.demoapp.domain.repository.MovieRepository
import kotlinx.coroutines.delay

class FakeMovieRepository : MovieRepository {

    override suspend fun getMovies(): List<Movie> {
        delay(700)
        val templates = listOf(
            Triple("Inception", "Sci-Fi", 2010),
            Triple("Interstellar", "Sci-Fi", 2014),
            Triple("The Dark Knight", "Action", 2008),
            Triple("The Matrix", "Sci-Fi", 1999),
            Triple("Parasite", "Thriller", 2019),
            Triple("Whiplash", "Drama", 2014)
        )

        return (1L..200L).map { id ->
            val template = templates[((id - 1) % templates.size).toInt()]
            val extraTitleIndex = ((id - 1) / templates.size + 1).toInt()
            val rating = 7.5 + ((id % 15).toDouble() / 10.0)

            Movie(
                id = id,
                title = "${template.first} #$extraTitleIndex",
                genre = template.second,
                year = template.third,
                rating = rating,
                imageUrl = "https://sun9-23.userapi.com/impg/W1PlmAbKmsLB9Yk-g7fcpI91JR0QnD7OszQ9sg/SmEGMH5_VxE.jpg?quality=90&proxy=1&sign=9fca5f4998f3605497e0ec63af3b27a7&type=video_thumb"
            )
        }
    }
}
