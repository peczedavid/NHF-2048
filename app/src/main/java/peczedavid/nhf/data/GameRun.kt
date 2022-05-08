package peczedavid.nhf.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Gamerun")
data class GameRun (
    @ColumnInfo(name="id") @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "points") var points: Int,
    @ColumnInfo(name = "seconds") var seconds: Int,
)