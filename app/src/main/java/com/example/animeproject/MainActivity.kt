package com.example.animeproject

import Anime
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import okhttp3.Headers
import org.json.JSONException
import java.io.Serializable

class MainActivity : AppCompatActivity() {

    private val allAnimeList = mutableListOf<Anime>()

    private lateinit var animeRecyclerView: RecyclerView
    private lateinit var animeAdapter: AnimeAdapter
    private lateinit var searchEditText: EditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var myListButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- 1. Initialize UI Components ---
        animeRecyclerView = findViewById(R.id.anime_recyclerview)
        searchEditText = findViewById(R.id.searchEditText)
        categoryChipGroup = findViewById(R.id.categoryChipGroup)
        myListButton = findViewById(R.id.myListButton)

        // --- 2. Setup RecyclerView and Adapter ---
        animeAdapter = AnimeAdapter(onFavoriteClicked = ::handleFavoriteClick)
        animeRecyclerView.adapter = animeAdapter
        animeRecyclerView.layoutManager = LinearLayoutManager(this)

        // --- 3. Setup UI Listeners ---
        setupSearchListener()
        setupChipGroupListener()
        setupMyListButtonListener()

        // --- 4. Fetch Initial Data ---
        fetchTopAnime()
    }

    override fun onResume() {
        super.onResume()
        // Refresh the displayed list every time the activity is shown.
        // This keeps the favorite statuses in sync.
        Log.d("MainActivity", "onResume: refreshing filters.")
        applyFilters()
    }

    /**
     * Handles the logic when a favorite button is clicked on an item.
     */
    private fun handleFavoriteClick(anime: Anime) {
        val animeInMasterList = allAnimeList.find { it.id == anime.id } ?: return

        animeInMasterList.isFavorite = !animeInMasterList.isFavorite

        if (animeInMasterList.isFavorite) {
            FavoritesRepository.addFavorite(anime.id)
            Log.d("MainActivity", "Added '${anime.title}' to favorites.")
        } else {
            FavoritesRepository.removeFavorite(anime.id)
            Log.d("MainActivity", "Removed '${anime.title}' from favorites.")
        }

        applyFilters()
    }

    private fun setupMyListButtonListener() {
        myListButton.setOnClickListener {
            val intent = Intent(this, MyListActivity::class.java)
            intent.putExtra("EXTRA_ANIME_LIST", ArrayList(allAnimeList))
            startActivity(intent)
        }
    }

    private fun setupSearchListener() {
        searchEditText.doOnTextChanged { _, _, _, _ -> applyFilters() }
    }

    private fun setupChipGroupListener() {
        categoryChipGroup.setOnCheckedStateChangeListener { _, _ -> applyFilters() }
    }

    /**
     * The filtering logic that runs whenever the UI needs to be updated.
     */
    private fun applyFilters() {
        val searchQuery = searchEditText.text.toString().trim().lowercase()
        val selectedGenres = categoryChipGroup.checkedChipIds.map { chipId ->
            findViewById<Chip>(chipId).text.toString().lowercase()
        }

        val filteredList = allAnimeList.filter { anime ->
            val matchesSearch = anime.title.lowercase().contains(searchQuery)
            val matchesGenre = selectedGenres.isEmpty() || anime.genre.any { it.lowercase() in selectedGenres }
            matchesSearch && matchesGenre
        }

        // KEY REFINEMENT: Submit a list of copied objects to force DiffUtil to update.
        animeAdapter.submitList(filteredList.map { it.copy() })
    }

    /**
     * Fetches the top anime from the Jikan API.
     */
    private fun fetchTopAnime() {
        val client = AsyncHttpClient()
        val url = "https://api.jikan.moe/v4/top/anime?limit=25"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                try {
                    val dataArray = json.jsonObject.getJSONArray("data")
                    val fetchedAnime = mutableListOf<Anime>()

                    for (i in 0 until dataArray.length()) {
                        val animeObject = dataArray.getJSONObject(i)
                        val id = animeObject.getString("mal_id")
                        val title = animeObject.optString("title_english", animeObject.getString("title"))
                        val synopsis = animeObject.optString("synopsis", "No description available.")
                        val imageUrl = animeObject.getJSONObject("images").getJSONObject("jpg").getString("large_image_url")

                        val genresArray = animeObject.getJSONArray("genres")
                        val genreList = mutableListOf<String>()
                        for (j in 0 until genresArray.length()) {
                            genreList.add(genresArray.getJSONObject(j).getString("name"))
                        }

                        // Check against the repository to set the initial favorite state
                        val isFavorite = FavoritesRepository.isFavorite(id)
                        fetchedAnime.add(Anime(id, title, genreList, synopsis, imageUrl, isFavorite))
                    }

                    allAnimeList.clear()
                    allAnimeList.addAll(fetchedAnime)
                    applyFilters()

                } catch (e: JSONException) {
                    Log.e("MainActivity", "Failed to parse JSON", e)
                }
            }

            override fun onFailure(statusCode: Int, headers: Headers?, response: String?, throwable: Throwable?) {
                Log.e("MainActivity", "API call failed with status code $statusCode", throwable)
            }
        })
    }
}
