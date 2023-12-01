package com.liuhaijia229350323.samplemusic

import android.app.Application
import com.liuhaijia229350323.samplemusic.data.MusicRepository
import com.liuhaijia229350323.samplemusic.data.MusicRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class SimpleMusicApplication: Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { MusicRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { MusicRepository(database.musicDao()) }
}