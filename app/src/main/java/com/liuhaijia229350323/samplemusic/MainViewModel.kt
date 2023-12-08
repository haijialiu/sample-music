package com.liuhaijia229350323.samplemusic

import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem

class MainViewModel : ViewModel() {

    // TODO: Implement the ViewModel
    var playItemMediaList: MutableList<MediaItem> = mutableListOf()
    fun addMediaItem(mediaItem: MediaItem) {
        playItemMediaList.add(mediaItem)
    }


}