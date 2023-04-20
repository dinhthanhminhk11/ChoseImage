package com.example.choseimage.adapter.image.impl

import android.net.Uri
import android.widget.ImageView
import coil.api.load
import coil.size.Scale
import com.example.choseimage.adapter.image.ImageAdapter

class CoilAdapter : ImageAdapter {
    override fun loadImage(target: ImageView, loadUrl: Uri) {
        target.load(loadUrl) {
            scale(Scale.FIT)
            scale(Scale.FILL)
        }
    }


    override fun loadDetailImage(target: ImageView, loadUrl: Uri) {
        target.load(loadUrl) {
            scale(Scale.FIT)
        }
    }
}