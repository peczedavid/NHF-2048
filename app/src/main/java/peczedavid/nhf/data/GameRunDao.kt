package peczedavid.nhf.data

import androidx.room.*
import peczedavid.nhf.data.GameRun

@Dao
interface GameRunDao {
    @Query("SELECT * FROM GameRun")
    fun getAll(): List<GameRun>

    @Query("DELETE FROM GameRun")
    fun clearTable()

    @Insert
    fun insert(gameRun: GameRun): Long

    @Update
    fun update(gameRun: GameRun)

    @Delete
    fun deleteRun(gameRun: GameRun)
}