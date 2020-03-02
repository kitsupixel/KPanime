package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseShow
import pt.kitsupixel.kpanime.database.entities.DatabaseShowAndMeta

@Dao
interface ShowDao {
    @Transaction
    @Query("SELECT * FROM shows WHERE active = 1 ORDER BY title")
    fun get(): LiveData<List<DatabaseShowAndMeta>?>

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    fun get(id: Long): LiveData<DatabaseShowAndMeta?>

    @Transaction
    @Query("SELECT * FROM shows WHERE id = :id")
    fun getObj(id: Long): DatabaseShowAndMeta?

    @Transaction
    @Query("SELECT shows.* FROM shows INNER JOIN show_meta ON shows.id = show_meta.show_id WHERE show_meta.favorite = 1 AND shows.active = 1 ORDER BY title")
    fun favorites(): LiveData<List<DatabaseShowAndMeta>?>

    @Transaction
    @Query("SELECT shows.* FROM shows INNER JOIN show_meta ON shows.id = show_meta.show_id WHERE show_meta.watched = 1 AND shows.active = 1 ORDER BY title")
    fun watched(): LiveData<List<DatabaseShowAndMeta>?>

    @Transaction
    @Query("SELECT * FROM shows WHERE ongoing = 1 AND shows.active = 1 ORDER BY title")
    fun current(): LiveData<List<DatabaseShowAndMeta>?>

    @Query("UPDATE show_meta SET favorite = NOT(favorite) WHERE show_id = :id")
    fun toggleFavorite(id: Long)

    @Query("UPDATE show_meta SET watched = NOT(watched) WHERE show_id = :id")
    fun toggleWatched(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg shows: DatabaseShow)

    @Update
    fun update(vararg shows: DatabaseShow)

    @Delete
    fun delete(vararg shows: DatabaseShow)
}