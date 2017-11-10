package tech.ketc.numeri.domain

import android.graphics.Bitmap
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.infra.element.MimeType
import java.io.File

interface IImageRepository {

    fun downloadOrGet(urlStr: String, cache: Boolean = true): BitmapContent

    fun save(bitmap: Bitmap, mimeType: MimeType, directory: String, fileName: String = currentDateStr, quality: Int = 100): File
}