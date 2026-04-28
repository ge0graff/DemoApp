package com.kochetkov.demoapp.presentation.movies

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kochetkov.demoapp.R
import com.kochetkov.demoapp.data.repository.FakeMovieRepository
import com.kochetkov.demoapp.domain.model.Movie
import com.kochetkov.demoapp.presentation.details.MovieDetailsFragment
import com.kochetkov.demoapp.presentation.movies.banner.PromoBannerDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MoviesFragment : Fragment(R.layout.fragment_movies) {

    private val viewModel: MoviesViewModel by viewModels {
        MoviesViewModel.Factory(FakeMovieRepository())
    }

    private val adapter = MoviesAdapter(
        onLikeClick = { movieId ->
            viewModel.accept(MoviesIntent.ToggleLike(movieId))
        },
        onOpenClick = { movie ->
            openMovieDetails(movie)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.moviesRecyclerView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val errorText: TextView = view.findViewById(R.id.errorText)
        val screenTitle: TextView = view.findViewById(R.id.screenTitle)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        errorText.setOnClickListener {
            viewModel.accept(MoviesIntent.Retry)
        }
        screenTitle.setOnLongClickListener {
            triggerCommitNowBackStackTrap()
            true
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        MoviesState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            errorText.visibility = View.GONE
                            errorText.text = ""
                        }

                        is MoviesState.Content -> {
                            progressBar.visibility = View.GONE
                            recyclerView.visibility = if (state.movies.isNotEmpty()) View.VISIBLE else View.GONE
                            errorText.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
                            errorText.text = state.errorMessage ?: ""
                            adapter.submitList(state.movies)
                        }
                    }
                }
            }
        }

        viewModel.accept(MoviesIntent.LoadMovies)
        if (savedInstanceState == null) {
            schedulePromoBanner()
        }
    }

    companion object {
        fun newInstance() = MoviesFragment()
    }

    private fun openMovieDetails(movie: Movie) {
        val detailsFragment = MovieDetailsFragment.newInstance(
            title = movie.title,
            genre = movie.genre,
            year = movie.year,
            rating = movie.rating,
            imageUrl = movie.imageUrl,
            recommendations = arrayListOf(
                Movie(
                    id = movie.id + 1000,
                    title = "${movie.title}: Director's Cut",
                    genre = movie.genre,
                    year = movie.year,
                    rating = (movie.rating + 0.1).coerceAtMost(10.0),
                    imageUrl = movie.imageUrl
                ),
                Movie(
                    id = movie.id + 1001,
                    title = "${movie.genre} pick of the week",
                    genre = movie.genre,
                    year = (movie.year - 1).coerceAtLeast(1980),
                    rating = movie.rating,
                    imageUrl = movie.imageUrl
                ),
                Movie(
                    id = movie.id + 1002,
                    title = "Classic from ${movie.year - 3}",
                    genre = "Drama",
                    year = movie.year - 3,
                    rating = (movie.rating - 0.2).coerceAtLeast(0.0),
                    imageUrl = movie.imageUrl
                ),
                Movie(
                    id = movie.id + 1003,
                    title = "Top rated similar film",
                    genre = "Action",
                    year = (movie.year + 1).coerceAtMost(2026),
                    rating = (movie.rating + 0.3).coerceAtMost(10.0),
                    imageUrl = movie.imageUrl
                )
            )
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.main, detailsFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun schedulePromoBanner() {
        lifecycleScope.launch {
            delay(3500L)
            showPromoBanner()
        }
    }

    private fun showPromoBanner() {
        if (parentFragmentManager.findFragmentByTag(PromoBannerDialogFragment.TAG) != null) {
            return
        }

        val promoBanner = PromoBannerDialogFragment()
        val transaction = parentFragmentManager.beginTransaction()
            .add(promoBanner, PromoBannerDialogFragment.TAG)
            .add(promoBanner, PromoBannerDialog2Fragment.TAG)
            .add(promoBanner, PromoBannerDialog3Fragment.TAG)

        if (parentFragmentManager.isStateSaved) {
            transaction.commitAllowingStateLoss()
        } else {
            transaction.commit()
        }
    }

    private fun triggerCommitNowBackStackTrap() {
        val fakeMovie = Movie(
            id = -1L,
            title = "Trap movie",
            genre = "Demo",
            year = 2026,
            rating = 10.0,
            imageUrl = "https://picsum.photos/600/400?grayscale"
        )

        val detailsFragment = MovieDetailsFragment.newInstance(
            title = fakeMovie.title,
            genre = fakeMovie.genre,
            year = fakeMovie.year,
            rating = fakeMovie.rating,
            imageUrl = fakeMovie.imageUrl,
            recommendations = arrayListOf(fakeMovie)
        )

        parentFragmentManager.beginTransaction()
            .replace(R.id.main, detailsFragment)
            .addToBackStack("commit_now_trap")
            .commitNow()
    }
}
