package pt.kitsupixel.kpanime.domain

data class Link(
    val id: Long,
    val episode_id: Long,
    val type: String,
    val quality: String,
    val link: String
)