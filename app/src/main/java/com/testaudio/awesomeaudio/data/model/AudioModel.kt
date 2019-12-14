package com.testaudio.awesomeaudio.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AudioModel(@PrimaryKey val fileName : String, val fileLocalPath: String, var fileURL : String? , val recordTime : String, var synced : Boolean)
