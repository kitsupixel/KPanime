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
    val link: String
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
            link = it.link
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
            link = it.link
        )
    }.toTypedArray()
}