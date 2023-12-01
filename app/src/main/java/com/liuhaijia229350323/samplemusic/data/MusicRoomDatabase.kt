package com.liuhaijia229350323.samplemusic.data

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = arrayOf(Music::class), version = 1, exportSchema = false)
public abstract class MusicRoomDatabase :RoomDatabase(){

   abstract fun musicDao():MusicDao

    companion object {
        @Volatile
        private var INSTANCE: MusicRoomDatabase?=null
        fun getDatabase(
            context:Context,
            scope: CoroutineScope
        ):MusicRoomDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicRoomDatabase::class.java,
                    "music_database"
                ).addCallback(WordDatabaseCallback(scope)).build()
                INSTANCE = instance
                instance
            }
        }

    }
    private class WordDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    initDatabase(database.musicDao())
                }
            }
        }

        suspend fun initDatabase(musicDao: MusicDao){
            musicDao.deleteAll()
            val music1 = Music(1,"周杰伦 - 告白气球","asset///music/周杰伦 - 告白气球_hires.flac")
            val music2 = Music(2,"YELL - 彼女がフラグを立てる理由 (她竖起旗帜的理由)","asset///music/YELL - 彼女がフラグを立てる理由 (她竖起旗帜的理由).flac")
            musicDao.insertAll(music1,music2)
        }
    }
}