package com.example.choseimage.ui.album.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.choseimage.BaseActivity
import com.example.choseimage.FishBun
import com.example.choseimage.Fishton
import com.example.choseimage.R
import com.example.choseimage.adapter.image.ImageAdapter
import com.example.choseimage.datasource.CameraDataSourceImpl
import com.example.choseimage.datasource.FishBunDataSourceImpl
import com.example.choseimage.datasource.ImageDataSourceImpl
import com.example.choseimage.ui.album.AlbumContract
import com.example.choseimage.ui.album.adapter.AlbumListAdapter
import com.example.choseimage.ui.album.listener.AlbumClickListener
import com.example.choseimage.ui.album.model.Album
import com.example.choseimage.ui.album.model.AlbumViewData
import com.example.choseimage.ui.album.model.repository.AlbumRepositoryImpl
import com.example.choseimage.ui.album.mvp.AlbumPresenter
import com.example.choseimage.ui.picker.PickerActivity
import com.example.choseimage.util.MainUiHandler
import com.example.choseimage.util.SingleMediaScanner
import com.example.choseimage.util.isLandscape
import com.example.choseimage.util.setStatusBarColor
import com.google.android.material.snackbar.Snackbar
import java.io.File

class AlbumActivity : BaseActivity(),
    AlbumContract.View, AlbumClickListener {
    private val albumPresenter: AlbumContract.Presenter by lazy {
        AlbumPresenter(
            this,
            AlbumRepositoryImpl(
                ImageDataSourceImpl(contentResolver),
                FishBunDataSourceImpl(Fishton),
                CameraDataSourceImpl(this)
            ),
            MainUiHandler()
        )
    }
    private var groupEmptyView: Group? = null
    private var recyclerAlbumList: RecyclerView? = null
    private var adapter: AlbumListAdapter? = null
    private var txtAlbumMessage: TextView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_album)
        initView()
        albumPresenter.getDesignViewData()
        if (checkPermission()) albumPresenter.loadAlbumList()
    }

    override fun onResume() {
        super.onResume()
        albumPresenter.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        albumPresenter.release()
    }

    private fun initView() {
        groupEmptyView = findViewById(R.id.group_album_empty)
        recyclerAlbumList = findViewById(R.id.recycler_album_list)
        txtAlbumMessage = findViewById(R.id.txt_album_msg)

        findViewById<ImageView>(R.id.iv_album_camera).setOnClickListener {
            albumPresenter.takePicture()
        }
    }

    override fun setRecyclerView(albumViewData: AlbumViewData) {
        val layoutManager =
            if (this.isLandscape()) GridLayoutManager(this, albumViewData.albumLandscapeSpanCount)
            else GridLayoutManager(this, albumViewData.albumPortraitSpanCount)

        recyclerAlbumList?.layoutManager = layoutManager
    }

    override fun setToolBar(albumViewData: AlbumViewData) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_album_bar)

        txtAlbumMessage?.setText(R.string.msg_loading_image)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(albumViewData.colorActionBar)
        toolbar.setTitleTextColor(albumViewData.colorActionBarTitle)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setStatusBarColor(albumViewData.colorStatusBar)
        }

        supportActionBar?.let {
            it.title = albumViewData.titleActionBar
            it.setDisplayHomeAsUpEnabled(true)
            if (albumViewData.drawableHomeAsUpIndicator != null) {
                it.setHomeAsUpIndicator(albumViewData.drawableHomeAsUpIndicator)
            }
        }

        if (albumViewData.isStatusBarLight && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        albumPresenter.getAlbumMenuViewData { albumMenuViewData ->
            if (albumMenuViewData.hasButtonInAlbumActivity) {
                menuInflater.inflate(R.menu.menu_photo_album, menu)
                val menuDoneItem = menu.findItem(R.id.action_done)
                menu.findItem(R.id.action_all_done).isVisible = false
                if (albumMenuViewData.drawableDoneButton != null) {
                    menuDoneItem.icon = albumMenuViewData.drawableDoneButton
                } else if (albumMenuViewData.strDoneMenu != null) {
                    if (albumMenuViewData.colorTextMenu != Int.MAX_VALUE) {
                        val spanString = SpannableString(albumMenuViewData.strDoneMenu)
                        spanString.setSpan(
                            ForegroundColorSpan(albumMenuViewData.colorTextMenu),
                            0,
                            spanString.length,
                            0
                        ) //fi
                        menuDoneItem.title = spanString
                    } else {
                        menuDoneItem.title = albumMenuViewData.strDoneMenu
                    }
                    menuDoneItem.icon = null
                }
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        } else if (id == R.id.action_done) {
            if (adapter != null) {
                albumPresenter.onClickMenuDone()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            ENTER_ALBUM_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    albumPresenter.finish()
                } else if (resultCode == TAKE_A_NEW_PICTURE_RESULT_CODE) {
                    albumPresenter.loadAlbumList()
                }
            }

            TAKE_A_PICTURE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    albumPresenter.onSuccessTakePicture()
                } else {
                    cameraUtil.savedPath?.let {
                        File(it).delete()
                    }
                }
            }
        }
    }

    override fun scanAndRefresh() {
        val savedPath = cameraUtil.savedPath ?: return

        SingleMediaScanner(this, File(savedPath)) {
            albumPresenter.loadAlbumList()
        }
    }

    override fun showNothingSelectedMessage(nothingSelectedMessage: String) {
        recyclerAlbumList?.let {
            it.post {
                Snackbar.make(it, nothingSelectedMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun showMinimumImageMessage(currentSelectedCount: Int) {
        recyclerAlbumList?.let {
            it.post {
                Snackbar
                    .make(
                        it,
                        getString(R.string.msg_minimum_image, currentSelectedCount),
                        Snackbar.LENGTH_SHORT
                    )
                    .show()
            }
        }
    }

    override fun takePicture(saveDir: String) {
        if (checkCameraPermission()) {
            cameraUtil.takePicture(this@AlbumActivity, saveDir, TAKE_A_PICTURE_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_STORAGE -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay!
                        albumPresenter.loadAlbumList()
                    } else {
                        permissionCheck.showPermissionDialog()
                        finish()
                    }
                }
            }
            PERMISSION_CAMERA -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, yay!
                        albumPresenter.takePicture()
                    } else {
                        permissionCheck.showPermissionDialog()
                    }
                }
            }
        }
    }

    override fun showAlbumList(
        albumList: List<Album>,
        imageAdapter: ImageAdapter,
        albumViewData: AlbumViewData
    ) {
        recyclerAlbumList?.visibility = View.VISIBLE
        groupEmptyView?.visibility = View.GONE
        setAlbumListAdapter(albumList, imageAdapter, albumViewData)
    }


    override fun onAlbumClick(position: Int, album: Album) {
        PickerActivity.getPickerActivityIntent(this, album.id, album.displayName, position)
            .also { startActivityForResult(it, ENTER_ALBUM_REQUEST_CODE) }
    }

    override fun changeToolbarTitle(selectedImageCount: Int, albumViewData: AlbumViewData) {
        supportActionBar?.apply {
            title =
                if (albumViewData.maxCount == 1 || !albumViewData.isShowCount) albumViewData.titleActionBar
                else getString(
                    R.string.title_toolbar,
                    albumViewData.titleActionBar,
                    selectedImageCount,
                    albumViewData.maxCount
                )
        }
    }

    override fun finishActivityWithResult(selectedImages: List<Uri>) {
        val i = Intent()
        i.putParcelableArrayListExtra(FishBun.INTENT_PATH, ArrayList(selectedImages))
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    private fun checkPermission(): Boolean {
        return permissionCheck.checkStoragePermission(PERMISSION_STORAGE)
    }

    private fun checkCameraPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionCheck.checkCameraPermission(PERMISSION_CAMERA)
        } else true
    }

    override fun setRecyclerViewSpanCount(albumViewData: AlbumViewData) {
        val recyclerView = recyclerAlbumList ?: return
        val gridLayoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return

        gridLayoutManager.spanCount =
            if (isLandscape()) albumViewData.albumLandscapeSpanCount
            else albumViewData.albumPortraitSpanCount
    }

    override fun refreshAlbumItem(position: Int, imagePath: ArrayList<Uri>) {
        val thumbnailPath = imagePath[imagePath.size - 1].toString()
        val addedCount = imagePath.size
        adapter?.updateAlbumMeta(0, addedCount, thumbnailPath)
        adapter?.updateAlbumMeta(position, addedCount, thumbnailPath)
    }

    override fun saveImageForAndroidQOrHigher() {
        val savedPath = cameraUtil.savedPath ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            cameraUtil.saveImageForAndroidQOrHigher(contentResolver, File(savedPath))
        }
    }


    private fun setAlbumListAdapter(
        albumList: List<Album>,
        imageAdapter: ImageAdapter,
        albumViewData: AlbumViewData
    ) {
        if (adapter == null) {
            adapter = AlbumListAdapter(this, albumViewData.albumThumbnailSize, imageAdapter).also {
                recyclerAlbumList?.adapter = it
            }
        }

        adapter?.let {
            it.setAlbumList(albumList)
            it.notifyDataSetChanged()
        }
    }

    override fun showEmptyView() {
        groupEmptyView?.visibility = View.VISIBLE
        recyclerAlbumList?.visibility = View.INVISIBLE
        txtAlbumMessage?.setText(R.string.msg_no_image)
    }
}