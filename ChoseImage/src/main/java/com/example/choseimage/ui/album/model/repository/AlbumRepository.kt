package com.example.choseimage.ui.album.model.repository

import android.net.Uri
import com.example.choseimage.adapter.image.ImageAdapter
import com.example.choseimage.ui.album.model.Album
import com.example.choseimage.ui.album.model.AlbumMenuViewData
import com.example.choseimage.ui.album.model.AlbumMetaData
import com.example.choseimage.ui.album.model.AlbumViewData
import com.example.choseimage.util.future.CallableFutureTask

interface AlbumRepository {
    fun getAlbumList(): CallableFutureTask<List<Album>>

    fun getAlbumMetaData(albumId: Long): CallableFutureTask<AlbumMetaData>

    fun getAlbumViewData(): AlbumViewData

    fun getImageAdapter(): ImageAdapter

    fun getSelectedImageList(): List<Uri>

    fun getAlbumMenuViewData(): AlbumMenuViewData

    fun getMessageNotingSelected(): String

    fun getMinCount(): Int

    fun getDefaultSavePath(): String?
}