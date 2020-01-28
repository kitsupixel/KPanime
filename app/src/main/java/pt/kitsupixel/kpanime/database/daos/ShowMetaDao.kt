package pt.kitsupixel.kpanime.database.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import pt.kitsupixel.kpanime.database.entities.DatabaseShow
import pt.kitsupixel.kpanime.database.entities.DatabaseShowAndMeta
import pt.kitsupixel.kpanime.database.entities.DatabaseShowMeta

@Dao
interface ShowMetaDao {
    @Query("SELECT * FROM show_meta WHERE show_id = :id")
    fun get(id: Long): DatabaseShowMeta?

    @Query("UPDATE show_meta SET favorite = NOT(favorite) WHERE show_id = :id")
    fun toggleFavorite(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg shows: DatabaseShowMeta)

    @Update
    fun update(vararg shows: DatabaseShowMeta)

    @Delete
    fun delete(vararg shows: DatabaseShowMeta)
}