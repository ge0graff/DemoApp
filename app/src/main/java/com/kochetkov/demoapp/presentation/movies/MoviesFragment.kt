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
import kotlinx.coroutines.launch

class MoviesFragment : Fragment(R.layout.fragment_movies) {

    private val viewModel: MoviesViewModel by viewModels {
        MoviesViewModel.Factory(FakeMovieRepository())
    }

    private val adapter = MoviesAdapter { movieId ->
        viewModel.accept(MoviesIntent.ToggleLike(movieId))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.moviesRecyclerView)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val errorText: TextView = view.findViewById(R.id.errorText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        errorText.setOnClickListener {
            viewModel.accept(MoviesIntent.Retry)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    recyclerView.visibility = if (state.movies.isNotEmpty()) View.VISIBLE else View.GONE

                    errorText.visibility = if (state.errorMessage != null) View.VISIBLE else View.GONE
                    errorText.text = state.errorMessage ?: ""

                    adapter.submitList(state.movies)
                }
            }
        }

        viewModel.accept(MoviesIntent.LoadMovies)
    }

    companion object {
        fun newInstance() = MoviesFragment()
    }
}
