package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import androidx.navigation.findNavController
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.databinding.FragmentPlayerBinding
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService
import com.liuhaijia229350323.samplemusic.session.MusicService

private const val TAG = "PlayerFragment"
class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicListViewModel:MusicListViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private lateinit var playerView: PlayerView
    private lateinit var playerBackImageButton: ImageButton

    private lateinit var controllerFuture: ListenableFuture<MediaController>

    companion object {
        fun newInstance() = PlayerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        musicListViewModel = ViewModelProvider(this,MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository))[MusicListViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]
        Log.d(TAG, "onCreate: playerViewModel: $playerViewModel")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater,container,false)
        val view = binding.root
        playerBackImageButton = binding.playerBack
        playerView = binding.playerView
        playerBackImageButton.setOnClickListener {
            view.findNavController().popBackStack()
            Log.d(TAG, "onCreateView: player back")
        }
        return view
    }


    override fun onStart() {
        super.onStart()
        context?.apply {
//            val sessionToken = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
            val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))

            controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener(
                {
                    // Call controllerFuture.get() to retrieve the MediaController.
                    // MediaController implements the Player interface, so it can be
                    // attached to the PlayerView UI component.
                    val controller = controllerFuture.get()
                    playerView.player = controller
                    Log.d(TAG, "onStart: this player is: $controller")
                },
                MoreExecutors.directExecutor()
            )
        }

    }
    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
    }


}