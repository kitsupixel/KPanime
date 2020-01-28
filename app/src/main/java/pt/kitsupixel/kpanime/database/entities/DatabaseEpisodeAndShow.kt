package pt.kitsupixel.kpanime.database.entities

import androidx.room.Embedded
import androidx.room.Relation
import pt.kitsupixel.kpanime.domain.EpisodeAndShow

data class DatabaseEpisodeAndShow (
    @Embedded val episode: DatabaseEpisode,
    @Relation(
        parentColumn = "show_id",
        entity = DatabaseShow::class,
        entityColumn = "id"
    )
    val show: DatabaseShow
)

fun List<DatabaseEpisodeAndShow>.episodeShowAsDomainModel(): List<EpisodeAndShow> {
    return map {
        EpisodeAndShow(
            episode = it.episode.episodeAsDomainModel(),
            show = it.show.showAsDomainModel()
        )
    }
}

fun DatabaseEpisodeAndShow.episodeShowAsDomainModel(): EpisodeAndShow {
    return EpisodeAndShow(
        episode = this.episode.episodeAsDomainModel(),
        show = this.show.showAsDomainModel()
    )
}

