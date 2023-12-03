package com.liuhaijia229350323.samplemusic


import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private val mediaSession: MediaSessionCompat? = null


    private lateinit var playerViewModel: PlayerViewModel
    private var mBound: Boolean = false
    private var player: ExoPlayer? = null
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val connection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            //val binder =  service as SimpleMusicService.LocalBinder
            //mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        setContentView(viewBinding.root)
        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        Log.d(TAG, "onCreate: playerViewModel: $playerViewModel")
        playerViewModel.player = this.player

//        val playerNotificationManager = PlayerNotificationManager(
//            this,
//        )


        val isFragmentContainerEmpty = savedInstanceState == null
//        if(isFragmentContainerEmpty){
//            supportFragmentManager
//                .beginTransaction()
//                .add(R.id.fragmentContainer,MusicListFragment.newInstance())
//                .commit()
//        }
        if(isFragmentContainerEmpty){
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer,PlayerFragment.newInstance())
                .commit()
        }


    }


    override fun onStart() {
        super.onStart()
        // 连接音频服务



//        if(player==null) {
//            initializePlayer()
//        }

    }

    override fun onResume() {
        super.onResume()
        //hideSystemUi()
//        if(player == null){
//            initializePlayer()
//        }
    }
//
    override fun onPause() {
        super.onPause()
//        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
//        unbindService(connection)
//        mBound = false
        //releasePlayer()
    }

//    private fun initializePlayer() {
//        player = ExoPlayer.Builder(this)
//            .build()
//            .also { exoPlayer ->
//                exoPlayer.playWhenReady = playWhenReady
//            }
//        Log.d(TAG, "initializePlayer: player initialize over, is $player")
//        player?.apply {
//            this.playWhenReady = playWhenReady
////            seekTo(currentWindow,playbackPosition)
//            prepare()
//        }
//        Log.d(TAG, "initializePlayer: player prepared.")
//    }

    private fun releasePlayer() {
        player?.run {
            playbackPosition = this.currentPosition
            currentWindow = this.currentMediaItemIndex
            playWhenReady = this.playWhenReady
            release()
        }
        player = null
    }
//    @SuppressLint("InlineApi")
//    private fun hideSystemUi(){
//        viewBinding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
//                or View.SYSTEM_UI_FLAG_FULLSCREEN
//                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
//    }
}