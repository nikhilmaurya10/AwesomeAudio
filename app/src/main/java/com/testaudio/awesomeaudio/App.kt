package com.testaudio.awesomeaudio

import android.app.Application
import net.gotev.uploadservice.Logger
import net.gotev.uploadservice.UploadService
import net.gotev.uploadservice.okhttp.OkHttpStack


class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // Gradle automatically generates proper variable as below.
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = OkHttpStack()
        Logger.setLogLevel(Logger.LogLevel.DEBUG)
    }
}