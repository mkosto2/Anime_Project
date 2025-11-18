package com.example.animeproject


object FavoritesRepository {

    // A private set to hold the unique IDs of the favorite anime.
    // Using a Set is efficient for checking if an item exists.
    private val favoriteIds = mutableSetOf<String>()


    fun addFavorite(animeId: String) {
        favoriteIds.add(animeId)
    }

    /**
     * Removes an anime's ID from the favorites list.
     */
    fun removeFavorite(animeId: String) {
        favoriteIds.remove(animeId)
    }

    /**
     * Checks if an anime is in the favorites list.
     */
    fun isFavorite(animeId: String): Boolean {
        return favoriteIds.contains(animeId)
    }

    /**
     * Gets the full list of favorite IDs.
     */
    fun getFavoriteIds(): Set<String> {
        return favoriteIds.toSet() // Return a copy for safety
    }
}
