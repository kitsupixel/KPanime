package pt.kitsupixel.kpanime.database.entities

import androidx.room.Embedded
import androidx.room.Relation
import pt.kitsupixel.kpanime.domain.Show

data class DatabaseShowAndMeta (
    @Embedded val show: DatabaseShow,
    @Relation(
        parentColumn = "id",
        entityColumn = "show_id",
        entity = DatabaseShowMeta::class
    )
    var meta: DatabaseShowMeta?
)

fun List<DatabaseShowAndMeta>.showMetaAsDomainModel(): List<Show> {
    return map {
        Show(
            id = it.show.id,
            title = it.show.title,
            synopsis = it.show.synopsis,
            thumbnail = it.show.thumbnail,
            season = it.show.season,
            year = it.show.year,
            ongoing = it.show.ongoing,
            favorite = it.meta?.favorite ?: false
        )
    }
}

fun DatabaseShowAndMeta.showMetaAsDomainModel(): Show {
    return  Show(
        id = this.show.id,
        title = this.show.title,
        synopsis = this.show.synopsis,
        thumbnail = this.show.thumbnail,
        season = this.show.season,
        year = this.show.year,
        ongoing = this.show.ongoing,
        favorite = this.meta?.favorite ?: false
    )
}