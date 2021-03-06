package net.ketc.numeri.util.android

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.MimeTypeFilter
import android.util.LruCache
import android.webkit.MimeTypeMap
import android.widget.ImageView
import io.reactivex.disposables.Disposable
import net.ketc.numeri.util.log.i
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


object ImageCache {
    private val MAX = 5 * 1024 * 1024
    private val URL_REGEX = "https?://[\\w/:%#$&?()~.=+\\-]+".toRegex()
    private val bitmapCache = object : LruCache<String, Bitmap>(MAX) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    private val queue: MutableList<Triple<String, (Bitmap) -> Unit, (Throwable) -> Unit>> = ArrayList()

    fun download(urlStr: String, cache: Boolean, autoDisposable: AutoDisposable, error: (Throwable) -> Unit, success: (Bitmap) -> Unit): Disposable? {
        if (!urlStr.matches(URL_REGEX)) {
            throw IllegalArgumentException("passed urlStr is not in the form of URL")
        }
        bitmapCache[urlStr]?.let {
            success(it)
            return null
        }
        if (queue.any { it.first == urlStr }) {
            queue.add(Triple(urlStr, success, error))
            return null
        } else {
            return autoDisposable.singleTask(MySchedulers.imageLoad) {
                var connection: HttpURLConnection? = null
                var inputStream: InputStream? = null
                return@singleTask try {
                    val url = URL(urlStr)
                    connection = url.openConnection()!! as HttpURLConnection
                    connection.allowUserInteraction = false
                    connection.instanceFollowRedirects = false
                    connection.requestMethod = "GET"
                    connection.connect()
                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.inputStream
                        BitmapFactory.decodeStream(inputStream)
                    } else {
                        throw RuntimeException("failed load image : responseCode = $responseCode")
                    }
                } finally {
                    inputStream?.close()
                    connection?.disconnect()
                }
            } error { throwable ->
                error(throwable)
                val filteredQueue = queue.filter { it.first == urlStr }
                queue.removeAll(filteredQueue)
                filteredQueue.forEach { it.third(throwable) }
            } success { bitmap ->
                fun put(): Bitmap {
                    bitmapCache.put(urlStr, bitmap)
                    return bitmap
                }
                success(if (cache) put() else bitmap)
                val filteredQueue = queue.filter { it.first == urlStr }
                queue.removeAll(filteredQueue)
                filteredQueue.forEach { it.second(bitmap) }
            }
        }
    }
}

fun ImageView.download(url: String, autoDisposable: AutoDisposable, cache: Boolean = true,
                       error: (Throwable) -> Unit = {},
                       success: () -> Unit = {}): Disposable? {
    this.setImageDrawable(null)
    return ImageCache.download(url, cache, autoDisposable, {
        this.setImageDrawable(null)
        error(it)
    }, {
        this.setImageBitmap(it)
        success()
    })
}

/**
 * @throws IOException directory creation failure
 */
fun ImageView.save() {
    val bitmapDrawable = this.drawable as? BitmapDrawable ?: throw IllegalStateException("bitmap is not set")
    bitmapDrawable.bitmap.save(context)
}

fun Bitmap.save(ctx: Context, mimeType: String = "image/png", quality: Int = 100): File {
    var outputStream: FileOutputStream? = null
    val dirName = "numetter"
    val file = File("${Environment.getExternalStorageDirectory().absolutePath}/$dirName")
    i("Bitmap", file.absolutePath)
    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()).format(Date()) + ".$extension"
    val path = "${file.absolutePath}/$fileName"
    try {
        file.takeIf { file.exists() || file.mkdir() }?.let {
            outputStream = FileOutputStream(path)
            val values = ContentValues()
            val resolver = ctx.contentResolver
            val format = when (mimeType) {
                "image/png" -> Bitmap.CompressFormat.PNG
                "image/jpeg" -> Bitmap.CompressFormat.JPEG
                else -> throw IllegalArgumentException()
            }
            compress(format, quality, outputStream)
            values.put(MediaStore.Images.Media.TITLE, fileName)
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            values.put(MediaStore.Images.Media.DATA, path)
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } ?: throw IOException("directory creation failure")
    } catch (e: IOException) {
        throw e
    } finally {
        outputStream?.flush()
        outputStream?.close()
    }
    return File(path)
}
