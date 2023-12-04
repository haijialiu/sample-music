package com.liuhaijia229350323.samplemusic


import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private val mediaSession: MediaSessionCompat? = null


    private lateinit var playerViewModel: PlayerViewModel


    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(viewBinding.root)
        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        Log.d(TAG, "onCreate: playerViewModel: $playerViewModel")


        val isFragmentContainerEmpty = savedInstanceState == null

        if(isFragmentContainerEmpty){
            val playerFragment = PlayerFragment.newInstance()
            val musicListFragment = MusicListFragment.newInstance()

            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragmentContainer, playerFragment)
                .hide( playerFragment)
                .add(R.id.fragmentContainer, musicListFragment)
                .commit()
        }
//        if(isFragmentContainerEmpty){
//            supportFragmentManager
//                .beginTransaction()
//                .add(R.id.fragmentContainer,PlayerFragment.newInstance())
//                .commit()
//        }


    }

    override fun onStart() {
        super.onStart()
//            val sessionToken = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
//            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//            controllerFuture.addListener(
//                {
//                    // Call controllerFuture.get() to retrieve the MediaController.
//                    // MediaController implements the Player interface, so it can be
//                    // attached to the PlayerView UI component.
//                    val controller = controllerFuture.get()
//
//
//                },
//                MoreExecutors.directExecutor()
//
//            )



    }


}