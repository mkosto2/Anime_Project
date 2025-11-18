package com.example.animeproject

import Anime
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button // <-- ADD THIS IMPORT
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.Serializable

class MyListActivity : AppCompatActivity() {

    private lateinit var myListRecyclerView: RecyclerView
    private lateinit var animeAdapter: AnimeAdapter

    private lateinit var discoverButton: Button
    private lateinit var myListButton: Button

    private var allAnimeList: List<Anime> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_list)

        allAnimeList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_ANIME_LIST", ArrayList::class.java) as? ArrayList<Anime> ?: listOf()
        } else {
            @Suppress("DEPRECATION", "UNCHECKED_CAST")
            intent.getSerializableExtra("EXTRA_ANIME_LIST") as? ArrayList<Anime> ?: listOf()
        }
        Log.d("MyListActivity", "onCreate: Received ${allAnimeList.size} total anime.")


        myListRecyclerView = findViewById(R.id.favoritesRecyclerView)
        animeAdapter = AnimeAdapter(onFavoriteClicked = ::handleFavoriteClicked)
        myListRecyclerView.adapter = animeAdapter
        myListRecyclerView.layoutManager = LinearLayoutManager(this)


        discoverButton = findViewById(R.id.discoverButton)
        myListButton = findViewById(R.id.myListButton)

        discoverButton.setOnClickListener {
            finish()
        }

        myListButton.isEnabled = false
        myListButton.alpha = 0.5f
    }

    override fun onResume() {
        super.onResume()
        Log.d("MyListActivity", "onResume: Refreshing favorites list.")
        loadFavoriteAnime()
    }

    private fun handleFavoriteClicked(anime: Anime) {
        FavoritesRepository.removeFavorite(anime.id)
        Log.d("MyListActivity", "Removed '${anime.title}' from favorites.")
        loadFavoriteAnime()
    }

    private fun loadFavoriteAnime() {
        val favoriteIds = FavoritesRepository.getFavoriteIds()
        val favoriteAnimes = allAnimeList.filter { anime ->
            favoriteIds.contains(anime.id)
        }
        Log.d("MyListActivity", "Displaying ${favoriteAnimes.size} favorite anime.")
        animeAdapter.submitList(favoriteAnimes.map { it.copy() })
    }
}
