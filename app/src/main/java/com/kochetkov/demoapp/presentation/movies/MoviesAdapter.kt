package com.kochetkov.demoapp.presentation.movies

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kochetkov.demoapp.R
import com.kochetkov.demoapp.domain.model.Movie
import kotlin.math.pow

class MoviesAdapter(]
    private val onLikeClick: (Long) -> Unit,
    private val onOpenClick: (Movie) -> Unit
) : ListAdapter<Movie, MoviesAdapter.MovieViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position), onLikeClick, onOpenClick)
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val genreText: TextView = itemView.findViewById(R.id.genreText)
        private val yearText: TextView = itemView.findViewById(R.id.yearText)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        private val viewsText: TextView = itemView.findViewById(R.id.viewsText)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)
        private val openButton: Button = itemView.findViewById(R.id.openButton)

        fun bind(movie: Movie, onLikeClick: (Long) -> Unit, onOpenClick: (Movie) -> Unit) {
            Glide.with(itemView)
                .load(movie.imageUrl)
                .centerCrop()
                .into(posterImage)
            titleText.text = movie.title
            genreText.text = movie.genre
            yearText.text = movie.year.toString()
            ratingText.text = movie.rating.toString()
            val position = bindingAdapterPosition
            val popularity = popularityByPosition.getOrPut(position) {
                calculatePopularity(movie)
            }
            viewsText.text = "Popularity: $popularity"
            if (movie.isLiked) {
                likeButton.setImageResource(R.drawable.ic_favorite_filled)
                likeButton.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.like_active)
                )
            } else {
                likeButton.setImageResource(R.drawable.ic_favorite_outline)
                likeButton.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.like_inactive)
                )
            }
            likeButton.setOnClickListener {
                onLikeClick(movie.id)
            }
            openButton.setOnClickListener {
                onOpenClick(movie)
            }
            startPosterPulseAnimation()
        }

        private fun startPosterPulseAnimation() {
            ObjectAnimator.ofPropertyValuesHolder(
                posterImage,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.12f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.12f, 1f)
            ).apply {
                duration = 850L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                start()
            }
        }

        companion object {
            private const val CURRENT_YEAR = 2026
            private val popularityByPosition = mutableMapOf<Int, Int>()

            private fun calculatePopularity(movie: Movie): Int {
                var popularity = 0.0
                repeat(7_000) { iteration ->
                    val weight = (iteration % 10 + 1) / 10.0
                    popularity += movie.rating * weight
                }

                val age = (CURRENT_YEAR - movie.year).coerceAtLeast(0)
                val agePenalty = (age + 1).toDouble().pow(1.2)
                return (popularity / agePenalty).toInt()
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}
