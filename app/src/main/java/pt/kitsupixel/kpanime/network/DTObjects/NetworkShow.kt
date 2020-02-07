package pt.kitsupixel.kpanime.network.DTObjects

import com.squareup.moshi.JsonClass
import pt.kitsupixel.kpanime.database.entities.DatabaseShow
import pt.kitsupixel.kpanime.domain.Show

@JsonClass(generateAdapter = true)
data class NetworkShowContainer(val data: List<NetworkShow>)

@JsonClass(generateAdapter = true)
data class NetworkShow(
    val id: Int,
    val title: String,
    val synopsis: String,
    val thumbnail: String,
    val season: String,
    val year: Int,
    val ongoing: Int,
    val active: Int
)

/**
 * Convert Network results to database objects
 */
fun NetworkShowContainer.asDomainModel(): List<Show> {
    return data.map {
        Show(
            id = it.id.toLong(),
            title = it.title,
            synopsis = it.synopsis,
            thumbnail = it.thumbnail,
            season = it.season,
            year = it.year,
            ongoing = it.ongoing == 1
        )
    }
}

fun NetworkShowContainer.asDatabaseModel(): Array<DatabaseShow> {
    return data.map {
        DatabaseShow(
            id = it.id.toLong(),
            title = it.title,
            synopsis = it.synopsis,
            thumbnail = it.thumbnail,
            season = it.season,
            year = it.year,
            ongoing = it.ongoing == 1,
            active = it.active == 1
        )
    }.toTypedArray()
}
