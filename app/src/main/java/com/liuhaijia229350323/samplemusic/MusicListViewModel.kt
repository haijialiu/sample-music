package com.liuhaijia229350323.samplemusic

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.liuhaijia229350323.samplemusic.data.Music
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class MusicListViewModel(private val repository: MusicRepository) : ViewModel() {
    // TODO: Implement the ViewModel
    val allMusics:LiveData<List<Music>> = repository.allMusic.asLiveData()
    fun insert(music: Music) = viewModelScope.launch {
        repository.insert(music)
    }
}
class MusicListViewModelFactory(private val repository: MusicRepository):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicListViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MusicListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}