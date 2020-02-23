package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndMeta
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndShow
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeMeta

@Dao
interface EpisodeMetaDao {

    @Query("SELECT * FROM episode_meta WHERE episode_id = :id")
    fun get(id: Long): DatabaseEpisodeMeta?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg shows: DatabaseEpisodeMeta)

    @Update
    fun update(vararg shows: DatabaseEpisodeMeta)

    @Delete
    fun delete(vararg shows: DatabaseEpisodeMeta)
}