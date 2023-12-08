package com.liuhaijia229350323.samplemusic.data

import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.MainFragmentDirections
import com.liuhaijia229350323.samplemusic.session.MusicService
import com.liuhaijia229350323.samplemusic.utils.LocalMusicLoader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
private const val TAG = "LocalMusicLoader"
@Database(entities = [Music::class], version = 1, exportSchema = false)
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
                ).addCallback(WordDatabaseCallback(scope,context)).build()
                INSTANCE = instance
                instance
            }
        }

    }
    private class WordDatabaseCallback(
        private val scope: CoroutineScope,
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    initDatabase(database.musicDao(), context)
                }
            }
        }

        private val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.IS_MUSIC,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DURATION,
        )
        private val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        suspend fun initDatabase(musicDao: MusicDao, context: Context) {
            musicDao.deleteAll()
            LocalMusicLoader(context)
            val query = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val displayNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val isMusicColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                Log.d(TAG, "read: $idColumn $titleColumn $artistColumn $albumColumn ")

                val filterTime = 60000 //60s
                val filterSize = 1 * 1024 //1mb

                while (cursor.moveToNext()) {
                    val isMusic = cursor.getInt(isMusicColumn)
                    if (isMusic == 0) {
                        continue
                    }
                    val duration = cursor.getLong(durationColumn)
                    if (duration < filterTime) {
                        continue
                    }
                    val fileSize = cursor.getLong(sizeColumn)
                    if (fileSize < filterSize) {
                        continue
                    }
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val album = cursor.getString(albumColumn)
                    val albumId = cursor.getLong(albumIdColumn)

                    val artworkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumCover = ContentUris.withAppendedId(artworkUri, albumId)

                    val path = cursor.getString(dataColumn)
                    val fileName = cursor.getString(displayNameColumn)

                    Log.d(
                        TAG,
                        "load: id: $id, title: $title, artist: $artist, album: $album, $albumCover, path: $path, fileName: $fileName "
                    )
                    musicDao.insertAll(Music(null, title, id, path))

                }
            }
        }
    }
}