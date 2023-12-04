package com.liuhaijia229350323.samplemusic.session

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

private const val TAG = "MusicService"
class MusicService: MediaLibraryService() {
    private var mediaLibrarySession: MediaLibrarySession? = null

    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MusicRoomDatabase.getDatabase(this,applicationScope) }
    private val repository by lazy { MusicRepository(database.musicDao()) }
    private var callback: MediaLibrarySession.Callback = object : MediaLibrarySession.Callback {
        // TODO: not do
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession


    @OptIn(UnstableApi::class) override fun onCreate() {
        super.onCreate()
        val defaultRenderersFactory= DefaultRenderersFactory(this).setExtensionRendererMode(
            DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
        )
        val player = ExoPlayer.Builder(this,defaultRenderersFactory).build()

        mediaLibrarySession = MediaLibrarySession.Builder(this, player, callback).build()

        Log.d(TAG, "onCreate: render count: ${player.rendererCount}")
        Log.d(TAG, "onCreate: ${player.getRenderer(0)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(1)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(2)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(3)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(4)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(5)}")

        repository.allMusic.asLiveData().observeForever(Observer { music ->
            music.forEach {
                val mediaItem = MediaItem.fromUri(it.musicUri)
                player.addMediaItem(mediaItem)

            }

            Log.d(TAG, "onCreate: get music: $music")
        })

        player.playWhenReady = true
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