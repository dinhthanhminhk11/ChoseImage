package com.example.choseimage.datasource

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.example.choseimage.MimeType
import com.example.choseimage.ext.equalsMimeType
import com.example.choseimage.ui.album.model.Album
import com.example.choseimage.ui.album.model.AlbumMetaData
import com.example.choseimage.util.future.CallableFutureTask
import java.util.ArrayList
import java.util.concurrent.Callable

class ImageDataSourceImpl(private val contentResolver: ContentResolver) : ImageDataSource {

    private val addedPathList = arrayListOf<Uri>()

    override fun getAlbumList(
        allViewTitle: String, exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<List<Album>> {
        return CallableFutureTask(Callable<List<Album>> {
            val albumDataMap = LinkedHashMap<Long, AlbumData>()
            val orderBy = "${MediaStore.Images.Media._ID} DESC"
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.BUCKET_ID
            )

            val c = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy)

            var totalCount = 0
            var allViewThumbnailPath: Uri = Uri.EMPTY

            c?.let {
                while (c.moveToNext()) {

                    val bucketId = c.getInt(c.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)).toLong()
                    val bucketDisplayName =
                        c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) ?: continue
                    val bucketMimeType = c.getString(c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)) ?: continue
                    val imgId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID))

                    if (isExceptImage(
                            bucketMimeType,
                            bucketDisplayName,
                            exceptMimeTypeList,
                            specifyFolderList
                        )
                    ) continue

                    val albumData = albumDataMap[bucketId]

                    if (albumData == null) {
                        val imagePath =
                            Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imgId)

                        albumDataMap[bucketId] =
                            AlbumData(
                                bucketDisplayName,
                                imagePath,
                                1
                            )

                        if (allViewThumbnailPath == Uri.EMPTY) allViewThumbnailPath = imagePath

                    } else {
                        albumData.imageCount++
                    }

                    totalCount++

                }
                c.close()
            }

            if (totalCount == 0) albumDataMap.clear()


            val albumList = ArrayList<Album>()

            if (!isNotContainsSpecifyFolderList(specifyFolderList, allViewTitle)
                && albumDataMap.isNotEmpty()
            )
                albumList.add(
                    0, Album(
                        0,
                        allViewTitle,
                        AlbumMetaData(
                            totalCount,
                            allViewThumbnailPath.toString()
                        )
                    )
                )

            albumDataMap.map {
                val value = it.value
                Album(
                    it.key,
                    value.displayName,
                    AlbumMetaData(
                        value.imageCount,
                        value.thumbnailPath.toString()
                    )
                )
            }.also {
                albumList.addAll(it)
            }

            albumList
        })
    }

    @SuppressLint("Range")
    override fun getAllBucketImageUri(
        bucketId: Long,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<List<Uri>> {
        return CallableFutureTask(Callable<List<Uri>> {
            val imageUris = arrayListOf<Uri>()
            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val bucketId: String = bucketId.toString()
            val sort = "${MediaStore.Images.Media._ID} DESC"
            val selectionArgs = arrayOf(bucketId)

            val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val c = if (bucketId != "0") {
                contentResolver.query(images, null, selection, selectionArgs, sort)
            } else {
                contentResolver.query(images, null, null, null, sort)
            }
            c?.let {
                try {
                    if (c.moveToFirst()) {
                        do {
                            val mimeType = c.getString(c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)) ?: continue
                            val folderName =
                                c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) ?: continue

                            if (isExceptMemeType(exceptMimeTypeList, mimeType)
                                || isNotContainsSpecifyFolderList(specifyFolderList, folderName)
                            ) continue

                            val imgId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID))
                            val path = Uri.withAppendedPath(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imgId
                            )
                            imageUris.add(path)
                        } while (c.moveToNext())
                    }
                } finally {
                    if (!c.isClosed) c.close()
                }
            }
            imageUris
        })
    }

    @SuppressLint("Range")
    override fun getAlbumMetaData(
        bucketId: Long,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ): CallableFutureTask<AlbumMetaData> {
        return CallableFutureTask(Callable<AlbumMetaData> {
            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val bucketId: String = bucketId.toString()
            val sort = "${MediaStore.Images.Media._ID} DESC"
            val selectionArgs = arrayOf(bucketId)

            val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val c = if (bucketId != "0") {
                contentResolver.query(images, null, selection, selectionArgs, sort)
            } else {
                contentResolver.query(images, null, null, null, sort)
            }

            var count = 0
            var thumbnailPath: Uri = Uri.EMPTY

            c?.let {
                try {
                    if (c.moveToFirst()) {
                        do {
                            val mimeType = c.getString(c.getColumnIndex(MediaStore.Images.Media.MIME_TYPE)) ?: continue
                            val folderName = c.getString(c.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)) ?: continue

                            if (isExceptMemeType(exceptMimeTypeList, mimeType)
                                || isNotContainsSpecifyFolderList(specifyFolderList, folderName)
                            ) continue

                            val imgId = c.getInt(c.getColumnIndex(MediaStore.MediaColumns._ID))
                            if (thumbnailPath == Uri.EMPTY) {
                                thumbnailPath =
                                    Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + imgId)
                            }
                            count++
                        } while (c.moveToNext())
                    }
                } finally {
                    if (!c.isClosed) c.close()
                }
            }
            AlbumMetaData(count, thumbnailPath.toString())
        })
    }

    @SuppressLint("Range")
    override fun getDirectoryPath(bucketId: Long): CallableFutureTask<String> {
        return CallableFutureTask(Callable<String> {
            var path = ""
            val selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
            val bucketId: String = bucketId.toString()
            val selectionArgs = arrayOf(bucketId)

            val images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val c = if (bucketId != "0") {
                contentResolver.query(images, null, selection, selectionArgs, null)
            } else {
                contentResolver.query(images, null, null, null, null)
            }
            c?.let {
                try {
                    if (c.moveToFirst()) {
                        path = getPathDir(
                            c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA)),
                            c.getString(c.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                        )
                    }
                } finally {
                    if (!c.isClosed) c.close()
                }
            }
            path
        })
    }

    override fun addAddedPath(addedImage: Uri) {
        addedPathList.add(addedImage)
    }

    override fun addAllAddedPath(addedImagePathList: List<Uri>) {
        addedPathList.addAll(addedImagePathList)
    }

    override fun getAddedPathList(): List<Uri> {
        return addedPathList
    }

    private fun getPathDir(path: String, fileName: String): String {
        return path.replace("/$fileName", "")
    }

    private fun isExceptMemeType(
        mimeTypes: List<MimeType>,
        mimeType: String
    ): Boolean {
        for (type in mimeTypes) {
            if (type.equalsMimeType(mimeType)) return true
        }
        return false
    }

    private fun isNotContainsSpecifyFolderList(
        specifyFolderList: List<String>,
        displayBundleName: String
    ): Boolean {
        return if (specifyFolderList.isEmpty()) false
        else !specifyFolderList.contains(displayBundleName)
    }

    private fun isExceptImage(
        bucketMimeType: String,
        bucketDisplayName: String,
        exceptMimeTypeList: List<MimeType>,
        specifyFolderList: List<String>
    ) = (isExceptMemeType(exceptMimeTypeList, bucketMimeType)
            || isNotContainsSpecifyFolderList(specifyFolderList, bucketDisplayName)
            )

    private data class AlbumData(
        val displayName: String,
        val thumbnailPath: Uri,
        var imageCount: Int
    )
}