package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.provider.Browser
import android.service.media.MediaBrowserService.BrowserRoot
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaBrowser.Listener
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.SessionToken
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.liuhaijia229350323.samplemusic.data.Music
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.databinding.FragmentMusicListBinding
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService
import com.liuhaijia229350323.samplemusic.session.MusicService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.log

private const val TAG = "MusicListFragment"

class MusicListFragment : Fragment() {
    private val musicListFragmentScope = CoroutineScope(SupervisorJob())
    private var _binding: FragmentMusicListBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicListRecyclerView: RecyclerView
    private lateinit var musicListViewModel: MusicListViewModel
    private lateinit var musicNumTextView: TextView


    private lateinit var controller: MediaController
    private lateinit var controllerFuture: ListenableFuture<MediaController>

    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null

    private lateinit var backTextView: TextView

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {

        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

        }
    }

    companion object {
        fun newInstance() = MusicListFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        musicListViewModel = ViewModelProvider(
            this,
            MusicListViewModelFactory((activity?.application as SimpleMusicApplication).repository)
        )[MusicListViewModel::class.java]

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicListBinding.inflate(inflater, container, false)
        val view = binding.root
        musicListRecyclerView = view.findViewById(R.id.music_list_recycler_view)
        musicListRecyclerView = binding.musicListRecyclerView
        musicNumTextView = binding.musicNumTextView
        backTextView = binding.backTextView
        backTextView.setOnClickListener(View.OnClickListener {
            view.findNavController().popBackStack()
        })
        val layoutManager = LinearLayoutManager(activity)
        musicListRecyclerView.layoutManager = layoutManager
        return view
    }


    override fun onStart() {
        super.onStart()
        musicListViewModel.playItemMediaList.observe(viewLifecycleOwner, Observer {
            musicListRecyclerView.adapter = MusicListAdapter(it)

        })
        requireContext().run {
            val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))

            controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
            controllerFuture.addListener(
                {

                    controller = controllerFuture.get()

                },
                MoreExecutors.directExecutor()
            )
            val mediaBrowserFuture = MediaBrowser.Builder(this, sessionToken).buildAsync()
            mediaBrowserFuture.addListener(
                {
                    val mediaBrowser = mediaBrowserFuture.get()
                    mediaBrowser.unsubscribe("[playListID]")
                    mediaBrowser.subscribe("[playListID]", null)

                    fun getPlayList() {
                        val defaultPlayListFuture = mediaBrowser.getChildren(
                            "[playListID]",
                            0,
                            Int.MAX_VALUE,
                            null
                        )
                        defaultPlayListFuture.addListener(
                            {
                                val defaultPlayListMediaItems =
                                    defaultPlayListFuture.get().value
                                defaultPlayListMediaItems?.let {
                                    musicListViewModel.playItemMediaList.postValue(it)
                                    musicNumTextView.text =
                                        getString(R.string.music_num, it.size)

                                }
                            }, MoreExecutors.directExecutor()
                        )
                    }
                    fun updatePlayList(){
                        val count = mediaBrowser.mediaItemCount
                        Log.d(TAG, "updatePlayList: current count $count")
                        val list = mutableListOf<MediaItem>()
                        for (i in 0 until count) {
                            list.add(i,mediaBrowser.getMediaItemAt(i))
                        }
                        musicListViewModel.playItemMediaList.postValue(list)
                        musicNumTextView.text =
                            getString(R.string.music_num, count)
                    }
                    mediaBrowser.addListener(object : Player.Listener {

                        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                            updatePlayList()
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            updatePlayList()
                        }
                        override fun onEvents(player: Player, events: Player.Events) {
                            updatePlayList()
                        }
                    })
                    updatePlayList()
                },
                MoreExecutors.directExecutor()
            )


        }
    }

    private inner class MusicListAdapter(private var musics: List<MediaItem>) :
        RecyclerView.Adapter<MusicListAdapter.MusicHolder>() {
        private inner class MusicHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleView: TextView = view.findViewById(R.id.title)
            val removeButton: ImageButton = view.findViewById(R.id.remove_music)
            var musicId: String? = null
            lateinit var musicItem: MediaItem

            init {
                // Define click listener for the ViewHolder's View.
                titleView.setOnClickListener(View.OnClickListener {
                    Log.d(
                        TAG, "titleView: ${titleView.text} click"
                    )
                    controller.setMediaItems(musics,layoutPosition,0)
                    //notify main fragment update ui
                    
                })
                removeButton.setOnClickListener(View.OnClickListener {
                    controller.removeMediaItem(layoutPosition)
                    notifyItemRemoved(layoutPosition)
                    Log.d(TAG, "remove music of list: removing $musicId\n and after: $musics")
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
            holder.musicItem = music
            holder.titleView.text = music.mediaMetadata.title
            holder.musicId = music.mediaId

        }


    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(controllerFuture)
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: destroy")
        super.onDestroy()
    }


}