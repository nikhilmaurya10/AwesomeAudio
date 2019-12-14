package com.testaudio.awesomeaudio.ui

import android.app.Application
import android.content.Context
import android.media.MediaRecorder
import android.util.Log
import androidx.lifecycle.*
import com.testaudio.awesomeaudio.data.AudioDB
import com.testaudio.awesomeaudio.data.model.AudioModel
import kotlinx.coroutines.launch
import net.gotev.uploadservice.*


class MainViewModel(application : Application, private val audiodb : AudioDB) : AndroidViewModel(application) {

    val uploadMap = mutableMapOf<String, String?>()
    private val state = MutableLiveData<AudioListState>().apply {
        value = AudioListState.Loading
    }

    val audioList : LiveData<List<AudioModel>> = Transformations.map(state) {
        state -> (state as? AudioListState.Result)?.data
    }

    private val showLoading : LiveData<Boolean> = Transformations.map(state) {
        state -> state is AudioListState.Loading
    }

    val showError : LiveData<Boolean> = Transformations.map(state) {
        state -> state is AudioListState.Error
    }

    val showData : LiveData<Boolean> = Transformations.map(state) {
        state -> state is AudioListState.Result
    }

    val storage = application.externalCacheDir?.absolutePath

    var mediaRecorder : MediaRecorder? = null

    var currentFile : AudioModel? = null

    init {
        getAllAudioList()
    }

    fun getAllAudioList() {

        val dao =  audiodb.getAudioDao()
        this.state.value = AudioListState.Loading

        viewModelScope.launch {
            state.value = AudioListState.Result(dao.getAllAudio())
        }
    }

    fun saveInDB() {

    }

    fun startRecording() {
        val currentTime = System.currentTimeMillis().toString()
        val path = "$storage/$currentTime.3gpp"
        currentFile = AudioModel(currentTime, path,null, currentTime, false)
        mediaRecorder = MediaRecorder().apply {

            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(path)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            }  catch (e : Exception) {
                print(e.message)
            }
            start()
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            currentFile?.let {
                viewModelScope.launch {
                    audiodb.getAudioDao().insert(it)
                    //refresh the list
                    getAllAudioList()
                    uploadAudio(it.fileLocalPath)
                }
            }

            print("File path>>>>>>>>>>>>${currentFile?.fileLocalPath}")
        }
        mediaRecorder = null

    }

    fun uploadAudio(path : String) {
        try {
            val uploadId = MultipartUploadRequest(
                getApplication(),
                "http://34.93.132.144:8080/api/uploadMedia"
            ).addFileToUpload(path, "media")
                .setNotificationConfig(UploadNotificationConfig())
                .setMaxRetries(2)
                .setDelegate(object : UploadStatusDelegate {
                    override fun onCancelled(context: Context?, uploadInfo: UploadInfo?) {
                        print("upload cancelled for ${uploadInfo?.uploadId}")
                    }

                    override fun onProgress(context: Context?, uploadInfo: UploadInfo?) {

                        print("upload progress for ${uploadInfo?.uploadId} is ${uploadInfo?.progressPercent}")
                    }

                    override fun onError(
                        context: Context?,
                        uploadInfo: UploadInfo?,
                        serverResponse: ServerResponse?,
                        exception: java.lang.Exception?
                    ) {

                        print("upload error for ${uploadInfo?.uploadId}")
                    }

                    override fun onCompleted(
                        context: Context?,
                        uploadInfo: UploadInfo?,
                        serverResponse: ServerResponse?
                    ) {

                        print("upload done for ${uploadInfo?.uploadId}")
                        print("server response ${serverResponse?.httpCode} ${serverResponse?.bodyAsString}")
                        updateStatus(uploadInfo?.uploadId)
                    }
                })
                .startUpload()
            uploadMap.set(uploadId, currentFile?.fileName)
        } catch (exc: java.lang.Exception) {
            Log.e("AndroidUploadService", exc.message, exc)
        }
    }

    private fun updateStatus(uploadId: String?) {
        val fileName = uploadMap[uploadId]
        viewModelScope.launch {
            fileName?.let {
                val audio = audiodb.getAudioDao().getByName(it)
                audio.synced = true
                audiodb.getAudioDao().insert(audio)
                getAllAudioList()
            }
        }

    }
}

class ViewModelFactory(private val application : Application, private val db: AudioDB) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(
            application, db
        ) as T
    }
}