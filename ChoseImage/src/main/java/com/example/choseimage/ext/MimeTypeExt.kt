package com.example.choseimage.ext

import com.example.choseimage.MimeType

fun MimeType.equalsMimeType(mimeType: String) = this.type == mimeType