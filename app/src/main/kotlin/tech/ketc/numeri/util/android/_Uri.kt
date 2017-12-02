package tech.ketc.numeri.util.android

import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import tech.ketc.numeri.infra.element.MimeType
import java.io.File
import java.io.IOException

private fun replace(str: String): String {
    var s = str
    if (s.matches(" *\\. *".toRegex())) {
        s = "_"
    }
    s = s.replace("<".toRegex(), "_")
    s = s.replace(">".toRegex(), "_")
    s = s.replace(":".toRegex(), "_")
    s = s.replace("\\*".toRegex(), "_")
    s = s.replace("\\?".toRegex(), "_")
    s = s.replace("\"".toRegex(), "_")
    s = s.replace("\\\\".toRegex(), "_")
    s = s.replace("/".toRegex(), "_")
    s = s.replace("\\|".toRegex(), "_")
    return s
}

fun createSaveUri(directory: String, fileName: String, mimeType: MimeType): Pair<Uri, String> {
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType.toString())
    val name = replace(fileName) + ".$extension"
    var directoryNames = directory.split("/")
    directoryNames = directoryNames.map(::replace)
    var directoryPath = Environment.getExternalStorageDirectory().absolutePath
    directoryNames.forEach { directoryName ->
        directoryPath += "/$directoryName"
        val file = File(directoryPath)
        if (!file.exists() && !file.mkdir()) throw IOException("directory creation failure")
    }
    val path = "${File(directoryPath).absolutePath}/$name"
    return Uri.fromFile(File(path)) to path
}