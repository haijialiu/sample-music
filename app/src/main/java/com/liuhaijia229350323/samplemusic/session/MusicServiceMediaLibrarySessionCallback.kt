package com.liuhaijia229350323.samplemusic.session

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.liuhaijia229350323.samplemusic.utils.LocalMusicLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
private const val TAG = "MusicServiceMediaLibrarySessionCallback"
open class MusicServiceMediaLibrarySessionCallback(context: Context) :
    MediaLibrarySession.Callback {

    init {
        MediaItemTree.initialize(LocalMusicLoader(context).load())
    }

    // 随机播放等按钮定制
    private val customLayoutCommandButtons: List<CommandButton> =
        listOf(
//            CommandButton.Builder()
        )
    @OptIn(UnstableApi::class)
    val mediaNotificationSessionCommands =
        MediaSession.ConnectionResult.DEFAULT_SESSION_AND_LIBRARY_COMMANDS.buildUpon()
            .also { builder ->
                customLayoutCommandButtons.forEach { commandButton ->
                    commandButton.sessionCommand?.let { builder.add(it) }
                }
            }
            .build()


    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        return super.onConnect(session, controller)
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
        return Futures.immediateFuture(LibraryResult.ofItem(MediaItemTree.getRootItem(),params))
    }

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val children = MediaItemTree.getChildren(parentId)
        if(children.isNotEmpty()){
            return Futures.immediateFuture(LibraryResult.ofItemList(children,params))
        }
        return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        Log.d(TAG, "onAddMediaItems: $mediaItems")
        return Futures.immediateFuture(resolveMediaItems(mediaItems))
    }
    private fun resolveMediaItems(mediaItems:List<MediaItem>):List<MediaItem>{
        val playlist = mutableListOf<MediaItem>()
        mediaItems.forEach{mediaItem ->
            if(mediaItem.mediaId.isNotEmpty()){
                MediaItemTree.expandItem(mediaItem)?.let{playlist.add(it)}
            } else if(mediaItem.requestMetadata.searchQuery != null){
                playlist.addAll(MediaItemTree.search(mediaItem.requestMetadata.searchQuery!!))
            }
        }
        return playlist
    }

    @OptIn(UnstableApi::class) // MediaSession.MediaItemsWithStartPosition
    private fun maybeExpandSingleItemToPlaylist(
        mediaItem: MediaItem,
        startIndex: Int,
        startPositionMs: Long
    ): MediaSession.MediaItemsWithStartPosition? {
        var playlist = listOf<MediaItem>()
        var indexInPlaylist = startIndex
        MediaItemTree.getItem(mediaItem.mediaId)?.apply {
            if (mediaMetadata.isBrowsable == true) {
                // Get children browsable item.
                playlist = MediaItemTree.getChildren(mediaId)
            } else if (requestMetadata.searchQuery == null) {
                // Try to get the parent and its children.
                MediaItemTree.getParentId(mediaId)?.let {
                    playlist =
                        MediaItemTree.getChildren(it).map { mediaItem ->
                            if (mediaItem.mediaId == mediaId) MediaItemTree.expandItem(mediaItem)!! else mediaItem
                        }
                    indexInPlaylist = MediaItemTree.getIndexInMediaItems(mediaId, playlist)
                }
            }
        }
        if (playlist.isNotEmpty()) {
            return MediaSession.MediaItemsWithStartPosition(
                playlist,
                indexInPlaylist,
                startPositionMs
            )
        }
        return null
    }

    override fun onSearch(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
        session.notifySearchResultChanged(browser,query,MediaItemTree.search(query).size,params)
        return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetSearchResult(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        query: String,
        page: Int,
        pageSize: Int,
        params: MediaLibraryService.LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        return Futures.immediateFuture(LibraryResult.ofItemList(MediaItemTree.search(query),params))
    }
    companion object {
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_ON =
            "android.media3.session.demo.SHUFFLE_ON"
        private const val CUSTOM_COMMAND_TOGGLE_SHUFFLE_MODE_OFF =
            "android.media3.session.demo.SHUFFLE_OFF"
    }
}
