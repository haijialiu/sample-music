package com.liuhaijia229350323.samplemusic

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.liuhaijia229350323.samplemusic.databinding.ActivityMainBinding
import com.liuhaijia229350323.samplemusic.databinding.FragmentPlayerBinding
private const val TAG = "PlayerFragment"
class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicListViewModel:MusicListViewModel

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition = 0L
    companion object {
        fun newInstance() = PlayerFragment()
    }

    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            player = ExoPlayer.Builder(it).build()

        }
        musicListViewModel = ViewModelProvider(this,MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository))[MusicListViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater,container,false)
        val view = binding.root
        playerView = binding.playerView
        playerView.player = player
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicListViewModel.allMusics.observe(viewLifecycleOwner,
            Observer {
                Log.d(TAG, "onViewCreated: $it")
                it.forEach { music ->
                    val mediaItem = MediaItem.fromUri(music.musicUri)
                    player?.addMediaItem(mediaItem)
                }
//                player?.apply {
//                    //setMediaItem(mediaItem)
//                    prepare()
//                    play()
//                }
//                val mediaItem = MediaItem.fromUri(it)
        })
//        val mediaItem1 = MediaItem.fromUri("asset:///music/YELL - 彼女がフラグを立てる理由 (她竖起旗帜的理由).flac")
//        val mediaItem2 = MediaItem.fromUri("asset:///music/周杰伦 - 告白气球_hires.flac")
//        val mediaItem3 = MediaItem.fromUri("asset:///music/榊原由依 (さかきばら ゆい) - My Wish Forever.ogg")
        player?.apply {
//            addMediaItem(mediaItem1)
//            addMediaItem(mediaItem2)
//            addMediaItem(mediaItem3)
            prepare()
            play()
        }

    }


}