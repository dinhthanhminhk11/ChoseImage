package com.example.choseimage.datasource

import com.example.choseimage.ui.picker.model.AlbumData

interface PickerIntentDataSource {
    fun getAlbumData(): AlbumData?
}