package com.ubuterm

import android.app.Application
import java.io.File

class UbutermApp : Application() {
    override fun onCreate() {
        super.onCreate()
        File(getExternalFilesDir(null), "rootfs").mkdirs()
        File(getExternalFilesDir(null), "archives").mkdirs()
        File(getExternalFilesDir(null), "logs").mkdirs()
    }
}
