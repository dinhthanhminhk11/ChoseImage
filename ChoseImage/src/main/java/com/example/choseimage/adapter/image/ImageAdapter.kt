package com.example.choseimage.adapter.image

import android.net.Uri
import android.widget.ImageView

interface ImageAdapter {
    fun loadImage(target: ImageView, loadUrl: Uri)
    fun loadDetailImage(target: ImageView, loadUrl: Uri)
}
