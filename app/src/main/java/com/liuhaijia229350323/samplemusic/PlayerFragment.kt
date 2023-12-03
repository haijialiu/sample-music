package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.databinding.FragmentPlayerBinding
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService

private const val TAG = "PlayerFragment"
class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicListViewModel:MusicListViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private lateinit var playerView: PlayerView

    companion object {
        fun newInstance() = PlayerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        musicListViewModel = ViewModelProvider(this,MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository))[MusicListViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]
        Log.d(TAG, "onCreate: playerViewModel: $playerViewModel")
        //先有activity 初始化播放器，所以不做空检查




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater,container,false)
        val view = binding.root
        playerView = binding.playerView
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
        context?.apply {
            val sessionToken = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
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
//            val sessionToken = SessionToken(requireContext(), ComponentName(this, SimpleMusicService::class.java))
//            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//            controllerFuture.addListener(
//                {
//                    // Call controllerFuture.get() to retrieve the MediaController.
//                    // MediaController implements the Player interface, so it can be
//                    // attached to the PlayerView UI component.
//                    playerView.player = controllerFuture.get()
//                },
//                MoreExecutors.directExecutor()
//            )
        }

    }


}