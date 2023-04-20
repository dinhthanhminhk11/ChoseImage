package com.example.choseimage.datasource

interface CameraDataSource {
    fun getCameraPath(): String
    fun getPicturePath(): String?
}