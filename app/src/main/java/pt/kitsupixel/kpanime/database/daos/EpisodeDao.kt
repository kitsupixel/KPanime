package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndLinks
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndShow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun get(id: Long): LiveData<DatabaseEpisode?>

    @Query("SELECT * FROM episodes WHERE show_id = :showId ORDER BY CASE type WHEN 'episode' THEN 1 WHEN 'batch' THEN 2 ELSE 3 END, CAST(number AS INT) DESC, released_on DESC")
    fun getByShow(showId: Long): LiveData<List<DatabaseEpisode>?>

    @Transaction
    @Query("SELECT * FROM episodes WHERE DATE(released_on) >= DATE('now', '-3 days') ORDER BY DATE(released_on) DESC, id DESC LIMIT 20")
    fun getLatest(): LiveData<List<DatabaseEpisodeAndShow>?>

    @Transaction
    @Query("SELECT * FROM episodes WHERE id = :id")
    fun getEpisodeAndLinks(id: Long): LiveData<DatabaseEpisodeAndLinks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg episodes: DatabaseEpisode)

    @Update
    fun update(vararg episodes: DatabaseEpisode)

    @Delete
    fun delete(vararg episode: DatabaseEpisode)
}