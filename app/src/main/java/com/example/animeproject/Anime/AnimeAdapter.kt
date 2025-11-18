package com.example.animeproject

import Anime
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AnimeAdapter(
    private val onFavoriteClicked: (Anime) -> Unit
) : ListAdapter<Anime, AnimeAdapter.AnimeViewHolder>(AnimeDiffCallback()) { // 1. Inherit from ListAdapter

    inner class AnimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.anime_title_text)
        private val genreTextView: TextView = itemView.findViewById(R.id.anime_genre_text)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.anime_description_text)
        private val animeImageView: ImageView = itemView.findViewById(R.id.anime_poster_image)
        private val favoriteButton: Button = itemView.findViewById(R.id.favorite_button)

        fun bind(anime: Anime) {
            titleTextView.text = anime.title
            genreTextView.text = anime.genre.joinToString(", ")
            descriptionTextView.text = anime.description

            Glide.with(itemView.context)
                .load(anime.imageUrl)
                .into(animeImageView)

            // The button text is determined by the item's state
            favoriteButton.text = if (anime.isFavorite) "Remove" else "Add"

            // Pass the specific anime item on click
            favoriteButton.setOnClickListener {
                onFavoriteClicked(anime)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_anime, parent, false)
        return AnimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnimeViewHolder, position: Int) {
        // 2. Get the item from the ListAdapter's internal list
        val anime = getItem(position)
        holder.bind(anime)
    }

    // 3. The "brain" that calculates list differences for automatic animations
    class AnimeDiffCallback : DiffUtil.ItemCallback<Anime>() {
        override fun areItemsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Anime, newItem: Anime): Boolean {
            return oldItem == newItem
        }
    }
}
