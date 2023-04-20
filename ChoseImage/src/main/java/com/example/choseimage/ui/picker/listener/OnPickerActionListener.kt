package com.example.choseimage.ui.picker.listener

interface OnPickerActionListener {
    fun takePicture()
    fun onDeselect()
    fun onClickImage(position: Int)
    fun onClickThumbCount(position: Int)
}