package pt.kitsupixel.kpanime.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import pt.kitsupixel.kpanime.domain.Link

@Entity(
    tableName = "episode_links",
    foreignKeys = [ForeignKey(
        entity = DatabaseEpisode::class,
        parentColumns = ["id"],
        childColumns = ["episode_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["episode_id"])]
)
data class DatabaseLink(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val episode_id: Long,
    val type: String,
    val quality: String,
    val language: String,
    val link: String,
    val seeds: Int,
    val leeches: Int,
    val downloads: Int
)

fun List<DatabaseLink>.linkAsDomainModel(): List<Link> {
    return map {
        Link(
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
    }
}

fun DatabaseLink.linkAsDomainModel(): Link {
    return Link(
        id = this.id,
        episode_id = this.episode_id,
        type = this.type,
        quality = this.quality,
        language = this.language,
        link = this.link,
        seeds = this.seeds,
        leeches = this.leeches,
        downloads = this.downloads
    )
}




