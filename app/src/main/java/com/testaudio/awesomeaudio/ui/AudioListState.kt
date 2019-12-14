package com.testaudio.awesomeaudio.ui

import com.testaudio.awesomeaudio.data.model.AudioModel

sealed class AudioListState {
    object Loading: AudioListState()
    class Result(val data : List<AudioModel>) : AudioListState()
    class Error(val error : Throwable?) : AudioListState()
}