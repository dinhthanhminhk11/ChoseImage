package com.example.choseimage.ui.album.model.repository

import android.os.Build
import com.example.choseimage.datasource.CameraDataSource
import com.example.choseimage.datasource.FishBunDataSource
import com.example.choseimage.datasource.ImageDataSource
import com.example.choseimage.ui.album.model.Album
import com.example.choseimage.ui.album.model.AlbumMetaData
import com.example.choseimage.ui.album.model.AlbumViewData
import com.example.choseimage.util.future.CallableFutureTask

class AlbumRepositoryImpl(
    private val imageDataSource: ImageDataSource,
    private val fishBunDataSource: FishBunDataSource,
    private val cameraDataSource: CameraDataSource
) : AlbumRepository {

    private var viewData: AlbumViewData? = null

    override fun getAlbumList(): CallableFutureTask<List<Album>> {
        return imageDataSource.getAlbumList(
            fishBunDataSource.getAllViewTitle(),
            fishBunDataSource.getExceptMimeTypeList(),
            fishBunDataSource.getSpecifyFolderList()
        )
    }

    override fun getAlbumMetaData(albumId: Long): CallableFutureTask<AlbumMetaData> {
        return imageDataSource.getAlbumMetaData(
            albumId,
            fishBunDataSource.getExceptMimeTypeList(),
            fishBunDataSource.getSpecifyFolderList()
        )
    }

    override fun getAlbumViewData(): AlbumViewData {
        return viewData ?: fishBunDataSource.getAlbumViewData().also { viewData = it }
    }

    override fun getImageAdapter() = fishBunDataSource.getImageAdapter()

    override fun getSelectedImageList() = fishBunDataSource.getSelectedImageList()

    override fun getAlbumMenuViewData() = fishBunDataSource.gatAlbumMenuViewData()

    override fun getMessageNotingSelected() = fishBunDataSource.getMessageNothingSelected()

    override fun getMinCount() = fishBunDataSource.getMinCount()

    override fun getDefaultSavePath(): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cameraDataSource.getPicturePath()
        } else {
            cameraDataSource.getCameraPath()
        }
    }
}