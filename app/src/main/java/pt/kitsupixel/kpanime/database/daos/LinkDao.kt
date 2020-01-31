package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseLink

@Dao
interface LinkDao {
    @Query("SELECT * FROM episode_links WHERE episode_id = :episodeId ORDER BY CASE quality WHEN '480p' THEN 1 WHEN '720p' THEN 2 WHEN '1080p' THEN 3 ELSE 0 END, CASE type WHEN 'Magnet' THEN 1 WHEN 'Torrent' THEN 2 WHEN 'XDCC' THEN 3 ELSE 4 END")
    fun getByEpisode(episodeId: Long): LiveData<List<DatabaseLink>?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg links: DatabaseLink)

    @Update
    fun update(vararg links: DatabaseLink)

    @Delete
    fun delete(vararg link: DatabaseLink)
}