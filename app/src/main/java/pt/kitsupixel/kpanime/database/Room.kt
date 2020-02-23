package pt.kitsupixel.kpanime.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pt.kitsupixel.kpanime.database.daos.*
import pt.kitsupixel.kpanime.database.entities.*

@Database(
    entities = [
        DatabaseShow::class,
        DatabaseEpisode::class,
        DatabaseLink::class,
        DatabaseShowMeta::class,
        DatabaseEpisodeMeta::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val showDao: ShowDao
    abstract val episodeDao: EpisodeDao
    abstract val linkDao: LinkDao
    abstract val showMetaDao: ShowMetaDao
    abstract val episodeMetaDao: EpisodeMetaDao
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
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .build()
        }
    }

    return INSTANCE
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE shows ADD COLUMN active INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE episodes ADD COLUMN type TEXT NOT NULL DEFAULT 'episode'")
        database.execSQL("ALTER TABLE episode_links ADD COLUMN language TEXT NOT NULL DEFAULT 'en'")
    }

}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE show_meta ADD COLUMN watched INTEGER NOT NULL DEFAULT 0")
        database.execSQL("""
        CREATE TABLE episode_meta (
            episode_id INTEGER PRIMARY KEY NOT NULL,
            downloaded INTEGER NOT NULL DEFAULT 0,
            watched INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent())
    }

}