package com.example.choseimage.datasource

import android.content.Context
import android.os.Environment

class CameraDataSourceImpl(private val context: Context) : CameraDataSource {
    override fun getCameraPath(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera").absolutePath
    }

    override fun getPicturePath(): String? {
        return context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath
    }


}