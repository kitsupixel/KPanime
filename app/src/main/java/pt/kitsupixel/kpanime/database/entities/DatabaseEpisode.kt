package pt.kitsupixel.kpanime.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import org.joda.time.DateTime
import pt.kitsupixel.kpanime.domain.Episode

@Entity(
    tableName = "episodes",
    foreignKeys = [ForeignKey(
        entity = DatabaseShow::class,
        parentColumns = ["id"],
        childColumns = ["show_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["show_id"])]
)
data class DatabaseEpisode constructor(
    @PrimaryKey(autoGenerate = false) val id: Long,
    val show_id: Long,
    val number: String,
    val released_on: String,
    val created_at: String
)

fun List<DatabaseEpisode>.episodeAsDomainModel(): List<Episode> {
    return map {
        Episode(
            id = it.id,
            show_id = it.show_id,
            number = it.number,
            released_on = it.released_on
        )
    }
}

fun DatabaseEpisode.episodeAsDomainModel(): Episode {
    return Episode(
        id = this.id,
        show_id = this.show_id,
        number = this.number,
        released_on = this.released_on
    )
}