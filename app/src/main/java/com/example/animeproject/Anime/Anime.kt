import java.io.Serializable
data class Anime(
    val id: String,
    val title: String,
    val genre: List<String>,
    val description: String,
    val imageUrl: String,
    var isFavorite: Boolean = false
) : Serializable
