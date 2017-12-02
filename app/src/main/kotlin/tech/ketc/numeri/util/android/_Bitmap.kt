package tech.ketc.numeri.util.android

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.infra.element.toFormat
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.File
import java.io.FileOutputStream
import java.util.*


fun Bitmap.save(ctx: Context, mimeType: MimeType, directory: String, fileName: String = Date().time.toString(), quality: Int): File {
    val (_, path) = createSaveUri(directory, fileName, mimeType)
    val name = File(path).name
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, name)
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType.toString())
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.DATA, path)
    }
    Logger.v(logTag, "mimeType${mimeType.toFormat()}")
    FileOutputStream(path).use { stream ->
        compress(mimeType.toFormat(), quality, stream)
    }
    ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    return File(path)
}