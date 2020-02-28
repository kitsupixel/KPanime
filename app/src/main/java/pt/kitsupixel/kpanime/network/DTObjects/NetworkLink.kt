package pt.kitsupixel.kpanime.network.DTObjects

import com.squareup.moshi.JsonClass
import pt.kitsupixel.kpanime.database.entities.DatabaseLink
import pt.kitsupixel.kpanime.domain.Link

@JsonClass(generateAdapter = true)
data class NetworkLinkContainer(val data: List<NetworkLink>)

@JsonClass(generateAdapter = true)
data class NetworkLink(
    val id: Long,
    val episode_id: Long,
    val type: String,
    val quality: String,
    val language: String,
    val link: String,
    val seeds: Int,
    val leeches: Int,
    val downloads: Int
)

/**
 * Convert Network results to database objects
 */
fun NetworkLinkContainer.asDomainModel(): List<Link> {
    return data.map {
        Link(
            id = it.id,
            episode_id = it.episode_id,
            type = it.type,
            quality = it.quality,
            language = it.language,
            link = it.link,
            seeds = it.seeds,
            leeches = it.leeches,
            downloads = it.leeches
        )
    }
}

fun NetworkLinkContainer.asDatabaseModel(): Array<DatabaseLink> {
    return data.map {
        DatabaseLink(
            id = it.id,
            episode_id = it.episode_id,
            type = it.type,
            quality = it.quality,
            language = it.language,
            link = it.link,
            seeds = it.seeds,
            leeches = it.leeches,
            downloads = it.downloads
        )
    }.toTypedArray()
}