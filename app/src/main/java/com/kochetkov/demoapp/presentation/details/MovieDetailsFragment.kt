package com.kochetkov.demoapp.presentation.details

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.kochetkov.demoapp.R
import com.kochetkov.demoapp.domain.model.Movie

class MovieDetailsFragment : Fragment(R.layout.fragment_movie_details) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText: TextView = view.findViewById(R.id.detailsTitleText)
        val genreText: TextView = view.findViewById(R.id.detailsGenreText)
        val yearText: TextView = view.findViewById(R.id.detailsYearText)
        val ratingText: TextView = view.findViewById(R.id.detailsRatingText)
        val posterImage: ImageView = view.findViewById(R.id.detailsPosterImage)
        val recommendationsContainer: LinearLayout =
            view.findViewById(R.id.recommendationsContainer)

        val title = requireArguments().getString(ARG_TITLE).orEmpty()
        val genre = requireArguments().getString(ARG_GENRE).orEmpty()
        val year = requireArguments().getInt(ARG_YEAR)
        val rating = requireArguments().getDouble(ARG_RATING)
        val imageUrl = requireArguments().getString(ARG_IMAGE_URL).orEmpty()
        val recommendations = getRecommendations(requireArguments())

        titleText.text = title
        genreText.text = getString(R.string.movie_genre_template, genre)
        yearText.text = getString(R.string.movie_year_template, year)
        ratingText.text = getString(R.string.movie_rating_template, rating)
        Glide.with(this).load(imageUrl).centerCrop().into(posterImage)

        recommendationsContainer.removeAllViews()
        recommendationsContainer.isVisible = recommendations.isNotEmpty()
        recommendations.forEach { recommendation ->
            val recommendationView = TextView(requireContext()).apply {
                text = getString(
                    R.string.recommendation_item_template,
                    "${recommendation.title} (${recommendation.year}) - ${recommendation.genre}"
                )
                setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            }
            recommendationsContainer.addView(recommendationView)
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_GENRE = "arg_genre"
        private const val ARG_YEAR = "arg_year"
        private const val ARG_RATING = "arg_rating"
        private const val ARG_IMAGE_URL = "arg_image_url"
        private const val ARG_RECOMMENDATIONS = "arg_recommendations"

        fun newInstance(
            title: String,
            genre: String,
            year: Int,
            rating: Double,
            imageUrl: String,
            recommendations: ArrayList<Movie>
        ): MovieDetailsFragment {
            return MovieDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_GENRE, genre)
                    putInt(ARG_YEAR, year)
                    putDouble(ARG_RATING, rating)
                    putString(ARG_IMAGE_URL, imageUrl)
                    putSerializable(ARG_RECOMMENDATIONS, recommendations)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getRecommendations(args: Bundle): ArrayList<Movie> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            args.getSerializable(ARG_RECOMMENDATIONS, ArrayList::class.java)
                ?.filterIsInstance<Movie>()
                ?.let(::ArrayList)
                ?: arrayListOf()
        } else {
            (args.getSerializable(ARG_RECOMMENDATIONS) as? ArrayList<*>)
                ?.filterIsInstance<Movie>()
                ?.let(::ArrayList)
                ?: arrayListOf()
        }
    }
}
