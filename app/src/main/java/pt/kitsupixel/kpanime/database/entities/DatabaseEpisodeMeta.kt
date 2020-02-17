package pt.kitsupixel.kpanime.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "episode_meta")
data class DatabaseEpisodeMeta constructor(
    @PrimaryKey val episode_id: Long,
    var downloaded: Boolean = false,
    var watched: Boolean = false
)