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
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.data.Music
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.databinding.FragmentMusicListBinding
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService
import com.liuhaijia229350323.samplemusic.session.MusicService

private const val TAG = "MusicListFragment"

class MusicListFragment : Fragment() {
    private var _binding:FragmentMusicListBinding? =null
    private val binding get() = _binding!!

    private lateinit var musicListRecyclerView: RecyclerView
    private lateinit var musicListViewModel: MusicListViewModel
    private lateinit var musicNumTextView: TextView
    private lateinit var controller: MediaController


    companion object {
        fun newInstance() = MusicListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        musicListViewModel = ViewModelProvider(
            this,
            MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository)
        )[MusicListViewModel::class.java]
//        musicListViewModel = ViewModelProvider(requireActivity(),MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository))[MusicListViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicListBinding.inflate(inflater,container,false)
        val view = binding.root
        musicListRecyclerView = view.findViewById(R.id.music_list_recycler_view)
        musicListRecyclerView = binding.musicListRecyclerView
        musicNumTextView = binding.musicNumTextView


        val layoutManager = LinearLayoutManager(activity)
        musicListRecyclerView.layoutManager = layoutManager
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        musicListViewModel.allMusics.observe(
            viewLifecycleOwner
        ) {
            Log.d(TAG, "onViewCreated: get all music from database: $it")
            musicListRecyclerView.adapter = MusicListAdapter(it)
            musicNumTextView.text = getString(R.string.music_num, it.size)
        }
    }

    override fun onStart() {
        super.onStart()
        context?.apply {
//            val sessionToken = SessionToken(this, ComponentName(this, MusicPlaybackService::class.java))
            val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
            val controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener(
                {
                    // Call controllerFuture.get() to retrieve the MediaController.
                    // MediaController implements the Player interface, so it can be
                    // attached to the PlayerView UI component.
                    controller = controllerFuture.get()
                    Log.d(TAG, "onStart: this player is: $controller")
                },
                MoreExecutors.directExecutor()

            )


        }
    }

    private class MusicListAdapter(private val musics: List<Music>) :
        RecyclerView.Adapter<MusicListAdapter.MusicHolder>() {
        private class MusicHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleView: TextView = view.findViewById(R.id.title)
            val removeButton: ImageButton = view.findViewById(R.id.remove_music)
            var musicId: Int? = null
            lateinit var musicUri:String
            init {
                // Define click listener for the ViewHolder's View.
                titleView.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "titleView: ${titleView.text} click\nready play music path: $musicUri")
                })
                removeButton.setOnClickListener(View.OnClickListener {
                    Log.d(TAG, "removeMusicButton: remove: $musicId")
                })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.music_row_item, parent, false)

            return MusicHolder(view)
        }

        override fun getItemCount() = musics.size

        override fun onBindViewHolder(holder: MusicHolder, position: Int) {
            val music = musics[position]
            Log.d(TAG, "onBindViewHolder: music: ${music.musicName}")
            holder.titleView.text = music.musicName
            holder.musicId = music.id
            holder.musicUri = music.musicUri
        }

    }

}