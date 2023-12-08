package com.liuhaijia229350323.samplemusic.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.liuhaijia229350323.samplemusic.data.Music
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private const val TAG = "LocalMusicLoader"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "version")
class LocalMusicLoader(private val context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MusicRoomDatabase.getDatabase(context, applicationScope) }
    private val repository by lazy { MusicRepository(database.musicDao()) }
    private val mediaVersion = stringPreferencesKey("MediaStoreVersion")

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
    suspend fun updateMediaStore(){
        val currentVersion = MediaStore.getVersion(this.context)
        val versionFlow: Flow<String> = context.dataStore.data
            .map {preferences ->
                preferences[mediaVersion] ?: ""
        }
        versionFlow.collect(){storeVersion ->
            Log.d(TAG, "updateMediaStore: currentVersion:$storeVersion - newVersion: $currentVersion")
            if(storeVersion== "" || storeVersion != currentVersion){
                Log.d(TAG, "updateMediaStore: updateData")
                context.dataStore.edit { version->
                    version[mediaVersion] = currentVersion
                }
                //TODO 更新本地存储



            }else{
                Log.d(TAG, "updateMediaStore: the MediaStoreVersion is the latest.")
            }
        }

    }
    suspend fun updateLocalMusic(musicList: List<Music>){
        musicList.forEach{
            repository.insert(it)
        }

    }


    fun load() : List<MediaItem>{

        val context = this.context
        val musicList:MutableList<MediaItem> = mutableListOf()
//        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)

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
//            Log.d(TAG, "read: $idColumn $titleColumn $artistColumn $albumColumn ")

            val filterTime = 60000 //60s
            val filterSize = 1*1024 //1mb

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

//                Log.d(TAG, "load: id: $id, title: $title, artist: $artist, album: $album, $albumCover, path: $path, fileName: $fileName ")
                val mediaItem =
                MediaItem.Builder().setMediaId(id.toString()).setUri(Uri.parse(path)).setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .setTitle(title)
                        .setArtist(artist)
                        .setAlbumTitle(album)
                        .setAlbumArtist(artist)
                        .setArtworkUri(albumCover)
                        .build()
                ).build()
                musicList.add(mediaItem)
            }
        }
        Log.d(TAG, "load ${musicList.size} items")
        return musicList
    }

    fun getMediaItemByMusic(music: Music) : MediaItem?{
        val where = "_id = ${music.mediaId}"
        val select = Bundle()
        select.putString(ContentResolver.QUERY_ARG_SQL_SELECTION,where)
        val cursor = this.context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            select,
            null
        )
        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val displayNameColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            Log.d(TAG, "getMediaItemByMusic: search ${cursor.count} result.")
            if(cursor.count == 1){
                while (cursor.moveToNext()) {
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
                        "search music success: id: $id, title: $title, artist: $artist, album: $album, $albumCover, path: $path, fileName: $fileName "
                    )
                    return MediaItem.Builder().setMediaId(id.toString()).setUri(Uri.parse(path))
                        .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(title)
                            .setArtist(artist)
                            .setAlbumTitle(album)
                            .setAlbumArtist(artist)
                            .setArtworkUri(albumCover)
                            .build()
                    ).build()
                }
            }

        }
        Log.d(TAG, "getMediaItemByMusic: no music: $music")
        return null
    }

}