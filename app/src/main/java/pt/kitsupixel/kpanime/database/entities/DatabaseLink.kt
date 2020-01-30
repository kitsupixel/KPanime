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
    val link: String
)

fun List<DatabaseLink>.linkAsDomainModel(): List<Link> {
    return map {
        Link(
            id = it.id,
            episode_id = it.episode_id,
            type = it.type,
            quality = it.quality,
            link = it.link
        )
    }
}

fun DatabaseLink.linkAsDomainModel(): Link {
    return Link(
        id = this.id,
        episode_id = this.episode_id,
        type = this.type,
        quality = this.quality,
        link = this.link
    )
}




