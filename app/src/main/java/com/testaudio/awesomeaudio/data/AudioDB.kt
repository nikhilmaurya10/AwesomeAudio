package com.testaudio.awesomeaudio.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.testaudio.awesomeaudio.data.model.AudioModel

@Database(entities = arrayOf(AudioModel::class), version = 1)
abstract  class AudioDB : RoomDatabase() {
    abstract fun getAudioDao() : AudioDao
}