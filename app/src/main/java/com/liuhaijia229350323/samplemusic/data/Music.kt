package com.liuhaijia229350323.samplemusic.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "musics")
data class Music(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val musicName: String,
    @ColumnInfo(name = "uri") val musicUri: String
)
