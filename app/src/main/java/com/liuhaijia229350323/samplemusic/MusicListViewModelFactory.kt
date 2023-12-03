package com.liuhaijia229350323.samplemusic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import java.lang.IllegalArgumentException

class MusicListViewModelFactory(private val repository: MusicRepository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MusicListViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return MusicListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}