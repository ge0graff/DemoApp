package com.kochetkov.demoapp.presentation.movies

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kochetkov.demoapp.R
import com.kochetkov.demoapp.domain.model.Movie

class MoviesAdapter(
    private val onLikeClick: (Long) -> Unit
) : ListAdapter<Movie, MoviesAdapter.MovieViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_card, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(getItem(position), onLikeClick)
    }

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val genreText: TextView = itemView.findViewById(R.id.genreText)
        private val yearText: TextView = itemView.findViewById(R.id.yearText)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)
        private val likeButton: ImageButton = itemView.findViewById(R.id.likeButton)

        fun bind(movie: Movie, onLikeClick: (Long) -> Unit) {
            Glide.with(itemView)
                .load(movie.imageUrl)
                .centerCrop()
                .into(posterImage)
            titleText.text = movie.title
            genreText.text = movie.genre
            yearText.text = movie.year.toString()
            ratingText.text = movie.rating.toString()
            if (movie.isLiked) {
                likeButton.setImageResource(R.drawable.ic_favorite_filled)
                likeButton.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.like_active)
                )
            } else {
                likeButton.setImageResource(R.drawable.ic_favorite_outline)
                likeButton.clearColorFilter()
            }
            likeButton.setOnClickListener {
                onLikeClick(movie.id)
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
