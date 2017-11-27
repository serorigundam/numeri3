package tech.ketc.numeri.util.android

import android.content.Context
import android.graphics.Bitmap
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.infra.element.toFormat
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.File
import java.io.FileOutputStream
import java.util.*


fun Bitmap.save(ctx: Context, mimeType: MimeType, directory: String, fileName: String = Date().time.toString(), quality: Int): File {
    val (_, path) = reserveContentUri(ctx, directory, fileName, mimeType)
    Logger.v(logTag, "mimeType${mimeType.toFormat()}")
    FileOutputStream(path).use { stream ->
        compress(mimeType.toFormat(), quality, stream)
    }
    return File(path)
}