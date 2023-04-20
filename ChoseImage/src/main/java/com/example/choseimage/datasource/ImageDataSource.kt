package com.example.choseimage.datasource

import android.net.Uri
import com.example.choseimage.MimeType
import com.example.choseimage.ui.album.model.Album
import com.example.choseimage.ui.album.model.AlbumMetaData
import com.example.choseimage.util.future.CallableFutureTask

interface ImageDataSource {
    fun getAlbumList(
        allViewTitle: String,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<List<Album>>

    fun getAllBucketImageUri(
        bucketId: Long,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<List<Uri>>

    fun getAlbumMetaData(
        bucketId: Long,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<AlbumMetaData>

    fun getDirectoryPath(bucketId: Long): CallableFutureTask<String>

    fun addAddedPath(addedImage: Uri)

    fun addAllAddedPath(addedImageList: List<Uri>)

    fun getAddedPathList(): List<Uri>
}