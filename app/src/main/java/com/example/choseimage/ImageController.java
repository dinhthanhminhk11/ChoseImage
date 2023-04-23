package com.example.choseimage;

import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import coil.Coil;
import coil.request.LoadRequest;
import coil.size.Scale;

public class ImageController {
    private ImageView imgMain;

    public ImageController(ImageView imgMain) {
        this.imgMain = imgMain;
    }

    public void setImgMain(Uri path) {
        Glide.with(imgMain.getContext())
                .load(path)
                .into(imgMain);
    }
}