package pt.kitsupixel.kpanime.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import pt.kitsupixel.kpanime.domain.Show

@Entity(tableName = "shows")
data class DatabaseShow constructor(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val title: String,
    val synopsis: String?,
    val thumbnail: String?,
    val season: String?,
    val year: Int?,
    val ongoing: Boolean = false,
    val active: Boolean = false
)

fun List<DatabaseShow>.showAsDomainModel(): List<Show> {
    return map {
        Show(
            id = it.id,
            title = it.title,
            synopsis = it.synopsis,
            thumbnail = it.thumbnail,
            season = it.season,
            year = it.year,
            ongoing = it.ongoing
        )
    }
}

fun DatabaseShow.showAsDomainModel(): Show {
    return  Show(
        id = this.id,
        title = this.title,
        synopsis = this.synopsis,
        thumbnail = this.thumbnail,
        season = this.season,
        year = this.year,
        ongoing = this.ongoing
    )
}