package com.liuhaijia229350323.samplemusic.session

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase
import com.liuhaijia229350323.samplemusic.utils.LocalMusicLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


private const val TAG = "MusicService"

class MusicService : MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val applicationScope = CoroutineScope(SupervisorJob())

    private lateinit var player: ExoPlayer

    private val database by lazy { MusicRoomDatabase.getDatabase(this, applicationScope) }
    private val repository by lazy { MusicRepository(database.musicDao()) }
    private val callback: MediaLibrarySession.Callback by lazy { createLibrarySessionCallback() }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession


    private fun createLibrarySessionCallback(): MediaLibrarySession.Callback {
        return MusicServiceMediaLibrarySessionCallback(this)
    }



    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        val defaultRenderersFactory = DefaultRenderersFactory(this).setExtensionRendererMode(
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        )


        player = ExoPlayer.Builder(this, defaultRenderersFactory).build()


        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback).build()
        mediaLibrarySession?.notifyChildrenChanged("[rootID]", Int.MAX_VALUE,null)


        val loader = LocalMusicLoader(this)
        applicationScope.launch {
            loader.updateMediaStore()
        }


        player.playWhenReady = false
        player.prepare()

    }

    override fun onDestroy() {
        mediaLibrarySession?.run {
            player.release()
            release()
            mediaLibrarySession = null
        }

        Log.d(TAG, "onDestroy: Music service destroy")
        super.onDestroy()
    }

}