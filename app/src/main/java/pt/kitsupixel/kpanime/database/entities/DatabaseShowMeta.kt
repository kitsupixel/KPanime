package pt.kitsupixel.kpanime.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "show_meta")
data class DatabaseShowMeta constructor(
    @PrimaryKey val show_id: Long,
    var favorite: Boolean = false
)