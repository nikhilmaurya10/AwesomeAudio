package com.testaudio.awesomeaudio.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.testaudio.awesomeaudio.data.model.AudioModel


@Dao
interface AudioDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(audioData : AudioModel)

    @Query("select * from audiomodel ")
    suspend fun getAllAudio() : List<AudioModel>

    @Query("select * from audiomodel where fileName = :name")
    suspend fun getByName(name: String) : AudioModel

    @Query("UPDATE audiomodel SET synced=:synced WHERE fileName = :fileName")
    fun update(synced: Boolean, fileName: String)
}