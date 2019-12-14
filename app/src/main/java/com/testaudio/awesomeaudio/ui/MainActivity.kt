package com.testaudio.awesomeaudio.ui

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.testaudio.awesomeaudio.R
import com.testaudio.awesomeaudio.data.AudioDB
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    var recording = false
    var player : MediaPlayer? = null

    lateinit var viewModel : MainViewModel
    val adapter = AudioListAdapter(this, emptyList(), ::playFile)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val db = Room.databaseBuilder(application, AudioDB::class.java, "audiodb").build()
        viewModel = ViewModelProviders.of(this, ViewModelFactory(application, db)).get(MainViewModel::class.java)
        setupUI()
        viewModel.audioList.observe(this,
            Observer {
                it?.let {
                    adapter.setNewList(it)
                }
            })
    }

    private fun setupUI() {
        val llm = LinearLayoutManager(this)
        recyclerView.layoutManager = llm
        recyclerView.adapter = adapter

        button.setOnClickListener{
            when (recording) {
                false -> checkPermissionAndStartRecording()
                true -> stopRecording()
            }
        }
    }

    private fun stopRecording() {
        viewModel.stopRecording()
        button.text = "Record New"
        recording = false
    }

    private fun checkPermissionAndStartRecording() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) { /* ... */
                    report?.areAllPermissionsGranted()?.let {
                        if (it) {
                            startRecording()
                        } else {
                            Toast.makeText(this@MainActivity , "Please Accept the permissions", Toast.LENGTH_LONG).show()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<com.karumi.dexter.listener.PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    checkPermissionAndStartRecording()
                }

            }).check()
    }

    private fun startRecording() {
        stopPlaying()
        button.text = "Recording... Press To stop!"
        recording = true
        viewModel.startRecording()
    }

    fun playFile(path: String) {
        if(recording) {
            Toast.makeText(this, "Cannot play file while recording", Toast.LENGTH_SHORT).show()
            return
        }
        startPlaying(path)
    }

    private fun startPlaying(path :String) {
        stopPlaying()
        player = MediaPlayer().apply {
            try {
                setDataSource(path)
                prepare()
                start()

            } catch (e: IOException) {
                print("prepare() failed")
            }
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

}
