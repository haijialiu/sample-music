package com.liuhaijia229350323.samplemusic


import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(viewBinding.root)
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if(player == null){
            initializePlayer()
        }
    }

    override fun onPause() {
        super.onPause()

    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                viewBinding.playerView.player = exoPlayer
                val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
                val theTestMediaUri = Uri.parse("asset:///music/周杰伦 - 告白气球_hires.flac")
                val theTestMedia = MediaItem.fromUri(theTestMediaUri)
                exoPlayer.setMediaItem(theTestMedia)
                exoPlayer.playWhenReady = playWhenReady
            }

        player?.apply {
            this.playWhenReady = playWhenReady
            seekTo(currentWindow,playbackPosition)
            prepare()
        }
    }
    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }

    @SuppressLint("InlineApi")
    private fun hideSystemUi(){
        viewBinding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }
}