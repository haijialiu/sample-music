package com.liuhaijia229350323.samplemusic

import android.content.ComponentName
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.liuhaijia229350323.samplemusic.session.MusicPlaybackService

class PlayerViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    var player: ExoPlayer? = null

}