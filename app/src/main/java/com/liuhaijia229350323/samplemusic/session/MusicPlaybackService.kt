package com.liuhaijia229350323.samplemusic.session


import android.content.Context
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi

import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

private const val TAG = "MusicPlaybackService"

class MusicPlaybackService : MediaSessionService(){
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { MusicRoomDatabase.getDatabase(this,applicationScope) }
    private val repository by lazy { MusicRepository(database.musicDao()) }
    private var mediaSession: MediaSession? = null

    private lateinit var player: ExoPlayer


    // Create your Player and MediaSession in the onCreate lifecycle event
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()


        val defaultRenderersFactory= DefaultRenderersFactory(this).setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
        player = ExoPlayer.Builder(this,defaultRenderersFactory)
//        player = ExoPlayer.Builder(this)
            .build()

        Log.d(TAG, "onCreate: ${player.getRenderer(0)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(1)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(2)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(3)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(4)}")
        Log.d(TAG, "onCreate: ${player.getRenderer(5)}")
        Log.d(TAG, "onCreate: render count: ${player.rendererCount}")


        player.addListener(object : Player.Listener{
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.albumTitle}")
            }
        })

        // read room database
        mediaSession = MediaSession.Builder(this, player).build()

        repository.allMusic.asLiveData().observeForever(Observer { music ->
            music.forEach {
                val mediaItem = MediaItem.fromUri(it.musicUri)
                player.addMediaItem(mediaItem)

            }

            Log.d(TAG, "onCreate: get music: $music")
        })

        player.playWhenReady = true
        player.prepare()
//        player.play()
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {

        return mediaSession
    }

}