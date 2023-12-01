package com.liuhaijia229350323.samplemusic.data


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface MusicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg musics: Music)
    @Delete
    suspend fun delete(music: Music)
    @Query("DELETE FROM musics")
    suspend fun deleteAll()
    @Query("SELECT * FROM musics")
    fun getAll(): Flow<List<Music>>
    @Query("")
    fun getMusicById(id:Int): Flow<Music>

}