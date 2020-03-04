package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndMeta
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndShow

@Dao
interface EpisodeDao {

    @Transaction
    @Query("SELECT * FROM episodes WHERE id = :id")
    fun get(id: Long): LiveData<DatabaseEpisodeAndMeta?>

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun getObj(id: Long): DatabaseEpisode?

    @Transaction
    @Query("SELECT * FROM episodes WHERE show_id = :showId ORDER BY CASE type WHEN 'episode' THEN 1 WHEN 'batch' THEN 2 ELSE 3 END, CAST(number AS INT) DESC, released_on DESC")
    fun getByShow(showId: Long): LiveData<List<DatabaseEpisodeAndMeta>?>

    @Transaction
    @Query("SELECT * FROM episodes WHERE DATE(released_on) >= DATE('now', '-3 days') ORDER BY DATE(released_on) DESC, id DESC LIMIT 20")
    fun getLatest(): LiveData<List<DatabaseEpisodeAndShow>?>

    @Query("UPDATE episode_meta SET watched = NOT(watched) WHERE episode_id = :id")
    fun toggleWatched(id: Long)

    @Query("UPDATE episode_meta SET downloaded = NOT(downloaded) WHERE episode_id = :id")
    fun toggleDownloaded(id: Long)

    @Query("UPDATE episode_meta SET downloaded = :state WHERE episode_id = :id")
    fun setDownloaded(id: Long, state: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vararg episodes: DatabaseEpisode): List<Long>

    @Update
    fun update(vararg episodes: DatabaseEpisode)

    @Delete
    fun delete(vararg episode: DatabaseEpisode)
}