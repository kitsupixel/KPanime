package pt.kitsupixel.kpanime.database.entities

import androidx.room.Embedded
import androidx.room.Relation
import pt.kitsupixel.kpanime.domain.EpisodeAndLink

data class DatabaseEpisodeAndLinks(
    @Embedded val episode: DatabaseEpisode,
    @Relation(
        parentColumn = "id",
        entity = DatabaseLink::class,
        entityColumn = "episode_id"
    )
    val links: List<DatabaseLink>
)

fun List<DatabaseEpisodeAndLinks>.episodeLinkAsDomainModel(): List<EpisodeAndLink> {
    return map {
        EpisodeAndLink(
            episode = it.episode.episodeAsDomainModel(),
            links = it.links.linkAsDomainModel()
        )
    }
}

fun DatabaseEpisodeAndLinks.episodeLinkAsDomainModel(): EpisodeAndLink {
    return EpisodeAndLink(
        episode = this.episode.episodeAsDomainModel(),
        links = this.links.linkAsDomainModel()
    )
}

