package com.liuhaijia229350323.samplemusic.session

import android.content.res.AssetManager
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.google.common.collect.ImmutableList
import java.lang.StringBuilder
private const val TAG = "MediaItemTree"
object MediaItemTree {
    private var treeNodes: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var titleMap: MutableMap<String, MediaItemNode> = mutableMapOf()
    private var isInitialized = false
    private const val ROOT_ID = "[rootID]"
    private const val DEFAULT_PLAY_LIST_ID = "[playListID]"
    private const val ALBUM_ID = "[albumID]"
    private const val GENRE_ID = "[genreID]"
    private const val ARTIST_ID = "[artistID]"
    private const val ALBUM_PREFIX = "[album]"
    private const val GENRE_PREFIX = "[genre]"
    private const val ARTIST_PREFIX = "[artist]"
    private const val ITEM_PREFIX = "[item]"

    private class MediaItemNode(val item: MediaItem) {
        val searchTitle = normalizeSearchText(item.mediaMetadata.title)
        val searchText =
            StringBuilder()
                .append(searchTitle)
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.subtitle))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.artist))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.albumArtist))
                .append(" ")
                .append(normalizeSearchText(item.mediaMetadata.albumTitle))
                .toString()

        private val children: MutableList<MediaItem> = ArrayList()
        fun addChild(childId: String) {
            this.children.add(treeNodes[childId]!!.item)
        }

        fun getChildren(): List<MediaItem> {
            return ImmutableList.copyOf(children)
        }
    }

    private fun buildMediaItem(
        title: String,
        mediaId: String,
        isPlayable: Boolean,
        isBrowsable: Boolean,
        mediaType: @MediaMetadata.MediaType Int,
        subtitleConfigurations: List<MediaItem.SubtitleConfiguration> = mutableListOf(),
        album: String? = null,
        artist: String? = null,
        genre: String? = null,
        sourceUri: Uri? = null,
        imageUri: Uri? = null
    ): MediaItem {
        val metadata =
            MediaMetadata.Builder()
                .setAlbumTitle(album)
                .setTitle(title)
                .setArtist(artist)
                .setGenre(genre)
                .setIsBrowsable(isBrowsable)
                .setIsPlayable(isPlayable)
                .setArtworkUri(imageUri)
                .setMediaType(mediaType)
                .build()

        return MediaItem.Builder()
            .setMediaId(mediaId)
            .setSubtitleConfigurations(subtitleConfigurations)
            .setMediaMetadata(metadata)
            .setUri(sourceUri)
            .build()
    }

    fun initialize(mediaItems: List<MediaItem>) {
        if (isInitialized) return
        isInitialized = true
        // create root and folders for album/artist/genre.
        treeNodes[ROOT_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Root Folder",
                    mediaId = ROOT_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
                )
            )
        treeNodes[DEFAULT_PLAY_LIST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Default Play List",
                    mediaId = DEFAULT_PLAY_LIST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_PLAYLIST
                )
            )
        treeNodes[ALBUM_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Album Folder",
                    mediaId = ALBUM_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS
                )
            )
        treeNodes[ARTIST_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Artist Folder",
                    mediaId = ARTIST_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS
                )
            )
        treeNodes[GENRE_ID] =
            MediaItemNode(
                buildMediaItem(
                    title = "Genre Folder",
                    mediaId = GENRE_ID,
                    isPlayable = false,
                    isBrowsable = true,
                    mediaType = MediaMetadata.MEDIA_TYPE_FOLDER_GENRES
                )
            )
        treeNodes[ROOT_ID]!!.addChild(ALBUM_ID)
        treeNodes[ROOT_ID]!!.addChild(ARTIST_ID)
        treeNodes[ROOT_ID]!!.addChild(GENRE_ID)
        treeNodes[ROOT_ID]!!.addChild(DEFAULT_PLAY_LIST_ID)
        mediaItems.forEach{
            addNodeToTree(it)
        }

    }
    //分类到每个播放列表
    private fun addNodeToTree(mediaObject:MediaItem){


        val id = mediaObject.mediaId
        val album = mediaObject.mediaMetadata.albumTitle
        val title = mediaObject.mediaMetadata.title
        val artist = mediaObject.mediaMetadata.artist
        val genre = mediaObject.mediaMetadata.genre

        val subtitleConfigurations: MutableList<MediaItem.SubtitleConfiguration> = mutableListOf()
        mediaObject.localConfiguration?.run {
                val subtitles = this.subtitleConfigurations
                for (i in 0 until subtitles.size) {
                    val subtitleObject = subtitles[i]
                    subtitleObject.language
                    subtitleConfigurations.add(
                        MediaItem.SubtitleConfiguration.Builder(subtitleObject.uri)
                            .setMimeType(mimeType)
                            .setLanguage(subtitleObject.language)
                            .build()
                    )
                }

        }


        // key of such items in tree
        val idInTree = ITEM_PREFIX + id
        val albumFolderIdInTree = ALBUM_PREFIX + album
        val artistFolderIdInTree = ARTIST_PREFIX + artist
        val genreFolderIdInTree = GENRE_PREFIX + genre


        treeNodes[idInTree] = MediaItemNode(mediaObject)

        titleMap[title.toString().lowercase()] = treeNodes[idInTree]!!




        if (!treeNodes.containsKey(albumFolderIdInTree)) {
            treeNodes[albumFolderIdInTree] =
                MediaItemNode(mediaObject)
            treeNodes[ALBUM_ID]!!.addChild(albumFolderIdInTree)
        }
        treeNodes[albumFolderIdInTree]!!.addChild(idInTree)

        // add into artist folder
        if (!treeNodes.containsKey(artistFolderIdInTree)) {
            treeNodes[artistFolderIdInTree] = MediaItemNode(mediaObject)
            treeNodes[ARTIST_ID]!!.addChild(artistFolderIdInTree)
        }
        treeNodes[artistFolderIdInTree]!!.addChild(idInTree)

        // add into genre folder
        if (!treeNodes.containsKey(genreFolderIdInTree)) {
            treeNodes[genreFolderIdInTree] = MediaItemNode(mediaObject)
            treeNodes[GENRE_ID]!!.addChild(genreFolderIdInTree)
        }

        treeNodes[genreFolderIdInTree]!!.addChild(idInTree)
        // add into default play list
        treeNodes[DEFAULT_PLAY_LIST_ID]?.addChild(idInTree)
    }
    fun getItem(id: String):MediaItem?{
//        return treeNodes["[item]$id"]?.item
        return treeNodes[id]?.item
    }
    fun expandItem(item: MediaItem): MediaItem? {

        var treeItem = getItem(item.mediaId)
        if (treeItem==null) {
            treeItem = getItem("[item]${item.mediaId}") ?: return null
        }
        @OptIn(UnstableApi::class) // MediaMetadata.populate
        val metadata = treeItem.mediaMetadata.buildUpon().populate(item.mediaMetadata).build()
        return item
            .buildUpon()
            .setMediaMetadata(metadata)
            .setSubtitleConfigurations(treeItem.localConfiguration?.subtitleConfigurations ?: listOf())
            .setUri(treeItem.localConfiguration?.uri)
            .build()
    }
    fun getParentId(mediaId:String,parentId:String = ROOT_ID):String?{
        for (child in treeNodes[parentId]!!.getChildren()) {
            if (child.mediaId == mediaId) {
                return parentId
            } else if (child.mediaMetadata.isBrowsable == true) {
                val nextParentId = getParentId(mediaId, child.mediaId)
                if (nextParentId != null) {
                    return nextParentId
                }
            }
        }
        return null
    }
    fun getIndexInMediaItems(mediaId: String, mediaItems: List<MediaItem>): Int {
        for ((index, child) in mediaItems.withIndex()) {
            if (child.mediaId == mediaId) {
                return index
            }
        }
        return 0
    }
    fun search(query: String): List<MediaItem> {
        val matches: MutableList<MediaItem> = mutableListOf()
        val titleMatches: MutableList<MediaItem> = mutableListOf()
        val words = query.split(" ").map { it.trim().lowercase() }.filter { it.length > 1 }
        titleMap.keys.forEach { title ->
            val mediaItemNode = titleMap[title]!!
            for (word in words) {
                if (mediaItemNode.searchText.contains(word)) {
                    if (mediaItemNode.searchTitle.contains(query.lowercase())) {
                        titleMatches.add(mediaItemNode.item)
                    } else {
                        matches.add(mediaItemNode.item)
                    }
                    break
                }
            }
        }
        titleMatches.addAll(matches)
        return titleMatches
    }
    fun getRootItem(): MediaItem {
        return treeNodes[ROOT_ID]!!.item
    }

    fun getChildren(id: String): List<MediaItem> {
        return treeNodes[id]?.getChildren() ?: listOf()
    }
    private fun normalizeSearchText(text: CharSequence?): String {
        if (text.isNullOrEmpty() || text.trim().length == 1) {
            return ""
        }
        return "$text".trim().lowercase()
    }
}