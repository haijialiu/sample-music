package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.liuhaijia229350323.samplemusic.databinding.FragmentMainBinding
import com.liuhaijia229350323.samplemusic.session.MusicService
import java.io.FileNotFoundException

private const val TAG = "MainFragment"

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var controller: MediaController
    private lateinit var musicControlButton: ImageButton
    private lateinit var musicTitleTextView: TextView
    private lateinit var musicListImageButton: ImageButton
    private lateinit var musicImageView: ImageView
    private var sessionToken: SessionToken? = null


    companion object {
        fun newInstance() = MainFragment()
    }

    //    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireContext().apply {
            XXPermissions.with(this)
                .permission(Permission.READ_MEDIA_AUDIO)
                .permission(Permission.NOTIFICATION_SERVICE)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        if (!allGranted) {
                            Log.d(TAG, "onGranted: permission not all request")
                            return
                        }
                        Log.d(TAG, "onGranted: permission all request")

                        connectMusicService()

                    }

                    override fun onDenied(
                        permissions: MutableList<String>,
                        doNotAskAgain: Boolean
                    ) {
                        if (doNotAskAgain) {
                            Log.d(TAG, "onDenied: do Not Ask Again")
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            //XXPermissions.startPermissionActivity(this, permissions)
                        } else {
                            Log.d(TAG, "onDenied: request failed")
                        }
                    }
                })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root
        musicControlButton = binding.musicController
        musicTitleTextView = binding.musicTitle
        musicImageView = binding.musicImage
        musicListImageButton = binding.musicList

        musicControlButton.setOnClickListener(View.OnClickListener {
            controller.playWhenReady = !controller.playWhenReady
            val newIcon: Drawable? = if (controller.playWhenReady) {
                ContextCompat.getDrawable(requireContext(), R.drawable.pause)
            } else {
                ContextCompat.getDrawable(requireContext(), R.drawable.play)
            }
            musicControlButton.setImageDrawable(newIcon)
        })

        musicTitleTextView.setOnClickListener {
            view.findNavController().navigate(R.id.openPlayer)
        }
        musicListImageButton.setOnClickListener {
            view.findNavController().navigate(R.id.openMusicList)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onStart() {
        super.onStart()
        sessionToken?.run {

        val controllerFuture = MediaController.Builder(requireContext(), this).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                val newIcon: Drawable? = if (controller.playWhenReady) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.pause)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.play)
                }
                musicControlButton.setImageDrawable(newIcon)
                controller.addListener(object : Player.Listener {
                    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {

                        Log.d(TAG, "onMediaMetadataChanged: ${mediaMetadata.artworkUri}")

                        val albumUri = mediaMetadata.artworkUri
                        albumUri?.run {
                            var bitmap = BitmapFactory.decodeStream(
                                requireContext().assets.open("no_album.png")
                            )
                            try {
                                val resolver = requireContext().contentResolver
                                resolver.openInputStream(this)?.use {
                                    bitmap = BitmapFactory.decodeStream(it)
                                }
                            } catch (e: FileNotFoundException) {
                                Log.w(TAG, "onMediaMetadataChanged: album file not found", e)
                            } finally {
                                musicImageView.setImageBitmap(bitmap)
                            }
                        }

                        musicTitleTextView.text = getString(
                            R.string.sample_music_title,
                            mediaMetadata.title,
                            mediaMetadata.artist
                        )
                    }
                })
                musicTitleTextView.text = getString(R.string.sample_music_title,
                    controller.mediaMetadata.title,
                    controller.mediaMetadata.artist
                )
            },
            MoreExecutors.directExecutor()
        )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun connectMusicService() {
        requireContext().apply {
            val token = SessionToken(this, ComponentName(this, MusicService::class.java))
            sessionToken = token

            val controllerFuture = MediaController.Builder(this, token).buildAsync()
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


}