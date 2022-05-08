package peczedavid.nhf.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [GameRun::class], version = 1)
abstract class LeaderboardDatabase : RoomDatabase() {
    abstract fun gameRunDao(): GameRunDao

    companion object {
        fun getDatabase(applicationContext: Context): LeaderboardDatabase {
            return Room.databaseBuilder(
                applicationContext,
                LeaderboardDatabase::class.java,
                "leaderboard"
            ).build()
        }
    }
}