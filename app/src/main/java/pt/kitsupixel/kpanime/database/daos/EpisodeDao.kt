package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisode
import pt.kitsupixel.kpanime.database.entities.DatabaseEpisodeAndShow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes WHERE id = :id")
    fun get(id: Int): LiveData<DatabaseEpisode?>

    @Query("SELECT * FROM episodes WHERE show_id = :showId ORDER BY released_on DESC")
    fun getByShow(showId: Long): LiveData<List<DatabaseEpisode>?>

    @Transaction
    @Query("SELECT * FROM episodes ORDER BY created_at DESC")
    fun getLatest(): LiveData<List<DatabaseEpisodeAndShow>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg episodes: DatabaseEpisode)

    @Update
    fun update(vararg episodes: DatabaseEpisode)

    @Delete
    fun delete(vararg episode: DatabaseEpisode)
}