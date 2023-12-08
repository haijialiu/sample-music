package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
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
import com.liuhaijia229350323.samplemusic.utils.LocalMusicLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

private const val TAG = "MainFragment"

class MainFragment : Fragment() {
    private val mainFragmentFragmentScope = CoroutineScope(SupervisorJob())
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var controller: MediaController
    private lateinit var musicControlButton: ImageButton
    private lateinit var musicTitleTextView: TextView
    private lateinit var musicListImageButton: ImageButton
    private lateinit var musicImageView: ImageView
    private lateinit var loadMusicButton: Button
    private var sessionToken: SessionToken? = null
    private lateinit var viewModel: MusicListViewModel


    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null


    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository)
        )[MusicListViewModel::class.java]
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
        loadMusicButton = binding.loadLocalMusicButton
        loadMusicButton.setOnClickListener {
            mainFragmentFragmentScope.launch {
                LocalMusicLoader(it.context).load()
            }
        }
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
        requireContext().run {
            val token = SessionToken(this, ComponentName(this, MusicService::class.java))
            val controllerFuture = MediaController.Builder(requireContext(), token).buildAsync()
            controllerFuture.addListener(
                {
                    controller = controllerFuture.get()
                    initializeBrowser()
                    val metadata = controller.mediaMetadata
                    updateMusicUi(metadata.artworkUri,metadata.title.toString(),metadata.artist.toString())

                },
                MoreExecutors.directExecutor()
            )
        }
    }

    private fun getBitmap(uri: Uri?): Bitmap {

        var bitmap = BitmapFactory.decodeStream(
            requireContext().assets.open("no_album.png")
        )
        if (uri == null) {
            return bitmap
        }
        try {
            val resolver = requireContext().contentResolver
            resolver.openInputStream(uri)?.use {
                bitmap = BitmapFactory.decodeStream(it)
            }
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "onMediaMetadataChanged: album file not found", e)
        }
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: destroy")
        super.onDestroy()

    }

    private fun initializeBrowser() {
        requireContext().run {
            browserFuture =
                MediaBrowser.Builder(
                    this,
                    SessionToken(this, ComponentName(this, MusicService::class.java))
                )
                    .buildAsync()
            browserFuture.addListener({ pushRoot() }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun pushRoot() {
        // browser can be initialized many times
        // only push root at the first initialization
        if (viewModel.playItemMediaList.isInitialized) {
            return
        }
        val browser = this.browser ?: return
        val rootFuture = browser.getLibraryRoot(/* params= */ null)
        rootFuture.addListener(
            {
                val childFuture = browser.getChildren("[playListID]", 0, Int.MAX_VALUE, null)
                val playListResult = childFuture.get()
                val playList = playListResult.value!!
                viewModel.playItemMediaList.postValue(playList)
                controller.addMediaItems(playList)
                controller.playWhenReady = false
                if(!playList.isEmpty()) {
                    updateMusicUi(playList[0])
                }
                Log.d(TAG, "pushRoot: ${viewModel.playItemMediaList}")

            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun updateMusicUi(mediaItem: MediaItem) {
        musicImageView.setImageBitmap(getBitmap(mediaItem.mediaMetadata.artworkUri))
        musicTitleTextView.text = getString(R.string.sample_music_title,mediaItem.mediaMetadata.title,mediaItem.mediaMetadata.artist)
    }
    private fun updateMusicUi(albumUri:Uri?,title:String?,artist:String?) {
        musicImageView.setImageBitmap(getBitmap(albumUri))
        musicTitleTextView.text = getString(R.string.sample_music_title,title?:"",artist?:"")
    }


}