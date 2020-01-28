package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseLink

@Dao
interface LinkDao {
    @Query("SELECT * FROM episode_links WHERE episode_id = :episodeId ORDER BY quality, type")
    fun getByEpisode(episodeId: Int): LiveData<List<DatabaseLink>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg links: DatabaseLink)

    @Update
    fun update(vararg links: DatabaseLink)

    @Delete
    fun delete(vararg link: DatabaseLink)
}