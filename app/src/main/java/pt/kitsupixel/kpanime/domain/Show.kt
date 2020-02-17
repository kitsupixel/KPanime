package pt.kitsupixel.kpanime.domain

data class Show(
    val id: Long,
    val title: String,
    val synopsis: String?,
    val thumbnail: String?,
    val season: String?,
    val year: Int?,
    val ongoing: Boolean,
    val favorite: Boolean = false,
    val watched: Boolean = false
)