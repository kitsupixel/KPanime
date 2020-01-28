package pt.kitsupixel.kpanime.network.DTObjects

import com.squareup.moshi.JsonClass
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.domain.Episode

@JsonClass(generateAdapter = true)
data class NetworkEpisodeContainer(val data: List<NetworkEpisode>)

@JsonClass(generateAdapter = true)
data class NetworkEpisode(
    val id: Long,
    val show_id: Long,
    val number: String,
    val released_on: String,
    val created_at: String
)

/**
 * Convert Network results to database objects
 */
fun NetworkEpisodeContainer.asDomainModel(): List<Episode> {
    return data.map {
        Episode(
            id = it.id,
            show_id = it.show_id,
            number = it.number,
            released_on = it.released_on
        )
    }
}

fun NetworkEpisodeContainer.asDatabaseModel(): Array<DatabaseEpisode> {
    return data.map {
        DatabaseEpisode(
            id = it.id,
            show_id = it.show_id,
            number = it.number,
            released_on = it.released_on,
            created_at = it.created_at
        )
    }.toTypedArray()
}