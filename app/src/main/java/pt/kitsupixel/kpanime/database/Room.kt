package pt.kitsupixel.kpanime.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pt.kitsupixel.kpanime.database.daos.EpisodeDao
import pt.kitsupixel.kpanime.database.daos.LinkDao
import pt.kitsupixel.kpanime.database.daos.ShowDao
import pt.kitsupixel.kpanime.database.daos.ShowMetaDao
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.database.entities.DatabaseLink
import pt.kitsupixel.kpanime.database.entities.DatabaseShow
import pt.kitsupixel.kpanime.database.entities.DatabaseShowMeta

@Database(
    entities = [
        DatabaseShow::class,
        DatabaseEpisode::class,
        DatabaseLink::class,
        DatabaseShowMeta::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val showDao: ShowDao
    abstract val episodeDao: EpisodeDao
    abstract val linkDao: LinkDao
    abstract val showMetaDao: ShowMetaDao
}

private lateinit var INSTANCE: AppDatabase

fun getDatabase(context: Context): AppDatabase {
    synchronized(lock = AppDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "db_shows"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    return INSTANCE
}