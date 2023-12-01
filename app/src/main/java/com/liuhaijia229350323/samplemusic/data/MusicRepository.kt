package com.liuhaijia229350323.samplemusic.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow


class MusicRepository(private val musicDao: MusicDao) {
    val allMusic: Flow<List<Music>> = musicDao.getAll()
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(music: Music){
        musicDao.insertAll(music)
    }
}