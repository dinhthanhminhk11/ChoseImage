package com.example.choseimage.util

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import java.io.File

class SingleMediaScanner @JvmOverloads constructor(
    context: Context,
    private val file: File,
    private val onScanCompleted: (() -> Unit)? = null) : MediaScannerConnection.MediaScannerConnectionClient {

    private val mediaScannerConnection: MediaScannerConnection = MediaScannerConnection(context, this)

    init {
        mediaScannerConnection.connect()
    }

    override fun onMediaScannerConnected() = mediaScannerConnection.scanFile(file.absolutePath, null)

    override fun onScanCompleted(path: String, uri: Uri?) {
        onScanCompleted?.invoke()
        mediaScannerConnection.disconnect()
    }
}
