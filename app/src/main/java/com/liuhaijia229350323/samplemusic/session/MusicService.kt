package com.liuhaijia229350323.samplemusic.session

import android.util.Log
import androidx.annotation.OptIn

import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase
import com.liuhaijia229350323.samplemusic.utils.LocalMusicLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob


private const val TAG = "MusicService"

class MusicService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MusicRoomDatabase.getDatabase(this, applicationScope) }
    private val repository by lazy { MusicRepository(database.musicDao()) }
    private var callback: MediaLibrarySession.Callback = object : MediaLibrarySession.Callback {
        // TODO: not do

    }


    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession


    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val defaultRenderersFactory = DefaultRenderersFactory(this).setExtensionRendererMode(
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        )


        val player = ExoPlayer.Builder(this, defaultRenderersFactory).build()

        player.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {

                Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.title}")
            }
        })
        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback).build()

        val loader = LocalMusicLoader(this)
        player.addMediaItems(loader.load())



        player.playWhenReady = false
        player.prepare()

    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }
        super.onDestroy()
    }
}