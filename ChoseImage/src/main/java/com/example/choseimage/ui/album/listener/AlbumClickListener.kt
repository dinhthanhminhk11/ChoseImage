package com.example.choseimage.ui.album.listener

import com.example.choseimage.ui.album.model.Album

interface AlbumClickListener {
    fun onAlbumClick(position: Int, album: Album)
}