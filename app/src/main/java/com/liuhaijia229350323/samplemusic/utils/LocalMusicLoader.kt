package com.liuhaijia229350323.samplemusic.utils

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata


private const val TAG = "LocalMusicLoader"
class LocalMusicLoader(private val context: Context) {
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

    fun load(context: Context = this.context) : List<MediaItem>{
        val musicList:MutableList<MediaItem> = mutableListOf()
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)





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

                Log.d(TAG, "load: id: $id, title: $title, artist: $artist, album: $album, $albumCover, path: $path, fileName: $fileName ")
//                val mediaItem = MediaItem.fromUri(Uri.parse(path))
                val mediaItem =
                MediaItem.Builder().setMediaId(id.toString()).setUri(Uri.parse(path)).setMediaMetadata(
                    MediaMetadata.Builder()
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
        return musicList
    }
}