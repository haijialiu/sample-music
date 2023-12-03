package com.liuhaijia229350323.samplemusic.session

import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.liuhaijia229350323.samplemusic.MusicListViewModel
import com.liuhaijia229350323.samplemusic.data.Music
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }
    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()

        mediaSession = MediaSession.Builder(this, player).build()
        repository.allMusic.asLiveData().observeForever(Observer { music ->
            music.forEach {
                player.addMediaItem(MediaItem.fromUri(it.musicUri))

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