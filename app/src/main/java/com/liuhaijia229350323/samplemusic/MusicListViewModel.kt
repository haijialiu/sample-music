package com.liuhaijia229350323.samplemusic

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.liuhaijia229350323.samplemusic.data.Music
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException
private const val TAG = "MusicListViewModel"
class MusicListViewModel(private val repository: MusicRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    private val viewModelScope = CoroutineScope(SupervisorJob())
    val allMusics:LiveData<List<Music>> = repository.allMusic.asLiveData()


    val playItemMediaList: MutableLiveData<List<MediaItem>> by lazy {
        MutableLiveData<List<MediaItem>>()
    }

    fun insert(music: Music) = viewModelScope.launch {
        repository.insert(music)
    }

    suspend fun deleteMusicByMediaItemId(mediaItemId: String?){
        if (mediaItemId==null) {
            Log.w(TAG, "deleteMusicByMediaItemId: mediaItemId is null")
            return
        }
        repository.deleteMusicByMediaItemId(mediaItemId)

    }

}
