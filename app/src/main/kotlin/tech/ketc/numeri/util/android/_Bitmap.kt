package tech.ketc.numeri.util.android

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.infra.element.toFormat
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


fun Bitmap.save(ctx: Context, mimeType: MimeType, directory: String, fileName: String = Date().time.toString(), quality: Int): File {
    val directoryNames = directory.split("/")
    var directoryPath = Environment.getExternalStorageDirectory().absolutePath
    directoryNames.forEach { directoryName ->
        directoryPath += "/$directoryName"
        val file = File(directoryPath)
        if (!file.exists() && !file.mkdir()) throw IOException("directory creation failure")
    }
    val file = File(directoryPath)
    val mimeTypeStr = mimeType.toString()
    Logger.v(logTag, "mimeType:$mimeTypeStr")
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeTypeStr)
    val path = "${file.absolutePath}/$fileName.$extension"
    Logger.v(logTag, "mimeType${mimeType.toFormat()}")
    FileOutputStream(path).use { stream ->
        compress(mimeType.toFormat(), quality, stream)
    }
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, fileName)
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.MIME_TYPE, mimeTypeStr)
        put(MediaStore.Images.Media.DATA, path)
    }
    ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    return File(path)
}