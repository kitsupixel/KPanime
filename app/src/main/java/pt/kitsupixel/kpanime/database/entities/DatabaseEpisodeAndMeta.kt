package pt.kitsupixel.kpanime.database.entities

import androidx.room.Embedded
import androidx.room.Relation
import pt.kitsupixel.kpanime.domain.Episode
import pt.kitsupixel.kpanime.domain.Show

data class DatabaseEpisodeAndMeta (
    @Embedded val episode: DatabaseEpisode,
    @Relation(
        parentColumn = "id",
        entityColumn = "episode_id",
        entity = DatabaseEpisodeMeta::class
    )
    var meta: DatabaseEpisodeMeta?
)

fun List<DatabaseEpisodeAndMeta>.episodeMetaAsDomainModel(): List<Episode> {
    return map {
        Episode(
            id = it.episode.id,
            show_id = it.episode.show_id,
            number = it.episode.number,
            type = it.episode.type,
            released_on = it.episode.released_on,
            downloaded = it.meta?.downloaded ?: false,
            watched = it.meta?.watched ?: false
        )
    }
}

fun DatabaseEpisodeAndMeta.episodeMetaAsDomainModel(): Episode {
        return Episode(
        id = this.episode.id,
        show_id = this.episode.show_id,
        number = this.episode.number,
        type = this.episode.type,
        released_on = this.episode.released_on,
        downloaded = this.meta?.downloaded ?: false,
        watched = this.meta?.watched ?: false
    )
}