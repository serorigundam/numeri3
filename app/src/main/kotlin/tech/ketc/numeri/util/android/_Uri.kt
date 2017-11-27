package tech.ketc.numeri.util.android

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import tech.ketc.numeri.infra.element.MimeType
import java.io.File
import java.io.IOException

fun reserveContentUri(ctx: Context, directory: String, fileName: String, mimeType: MimeType): Pair<Uri, String> {
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType.toString())
    val name = fileName + ".$extension"
    val directoryNames = directory.split("/")
    var directoryPath = Environment.getExternalStorageDirectory().absolutePath
    directoryNames.forEach { directoryName ->
        directoryPath += "/$directoryName"
        val file = File(directoryPath)
        if (!file.exists() && !file.mkdir()) throw IOException("directory creation failure")
    }
    val path = "${File(directoryPath).absolutePath}/$name"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.TITLE, name)
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        put(MediaStore.Images.Media.DATA, path)
    }
    return ctx.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) to path
}