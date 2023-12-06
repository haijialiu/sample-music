package com.liuhaijia229350323.samplemusic

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.Manifest.permission.READ_MEDIA_VIDEO
import android.Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
import android.content.ComponentName
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
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding
import com.liuhaijia229350323.samplemusic.session.MusicService


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {


    private val mediaSession: MediaSessionCompat? = null


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
//        imageButton = viewBinding.musicController
//        imageButton.setOnClickListener(View.OnClickListener {
//            controller.playWhenReady = !controller.playWhenReady
//            val newIcon: Drawable? = if (controller.playWhenReady) {
//                ContextCompat.getDrawable(this, R.drawable.pause)
//            } else {
//                ContextCompat.getDrawable(this, R.drawable.play)
//            }
//            imageButton.setImageDrawable(newIcon)
//        })

    }


    override fun onStart() {
        super.onStart()
//        XXPermissions.with(this)
//            .permission(Permission.READ_MEDIA_AUDIO)
//            .permission(Permission.NOTIFICATION_SERVICE)
//            .request(object : OnPermissionCallback {
//                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
//                    if (!allGranted) {
//                        Log.d(TAG, "onGranted: permission not all request")
//                        return
//                    }
//                    Log.d(TAG, "onGranted: permission all request")
//                    connectMusicService()
//                }
//
//                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
//                    if (doNotAskAgain) {
//                        Log.d(TAG, "onDenied: do Not Ask Again")
//                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
//                        //XXPermissions.startPermissionActivity(this, permissions)
//                    } else {
//                        Log.d(TAG, "onDenied: request failed")
//                    }
//                }
//            })

    }
    fun connectMusicService(){
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                // Call controllerFuture.get() to retrieve the MediaController.
                // MediaController implements the Player interface, so it can be
                // attached to the PlayerView UI component.
                controller = controllerFuture.get()

            },
            MoreExecutors.directExecutor()

        )
    }


}