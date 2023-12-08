package com.liuhaijia229350323.samplemusic

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding
import com.liuhaijia229350323.samplemusic.session.MusicService


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {


    private lateinit var playerViewModel: PlayerViewModel
    private lateinit var controller: MediaController


    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }
//    private lateinit var imageButton: ImageButton


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(viewBinding.root)
        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        Log.d(TAG, "onCreate: playerViewModel: $playerViewModel")


    }



    fun connectMusicService(){
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {

                controller = controllerFuture.get()

            },
            MoreExecutors.directExecutor()

        )
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        Log.d(TAG, "onDestroy: main activity is destroy")
        super.onDestroy()
        val intent = Intent(this,MusicService::class.java).apply {

        }
        stopService(intent)
//        android.os.Process.killProcess(android.os.Process.myPid())
    }


}