package net.ketc.numeri.util.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.MySchedulers
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


object ImageCache {
    private val MAX = 5 * 1024 * 1024
    private val URL_REGEX = "https?://[\\w/:%#$&?()~.=+\\-]+".toRegex()
    val bitmapCache = object : LruCache<String, Bitmap>(MAX) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount
        }
    }

    val queue: MutableList<Triple<String, (Bitmap) -> Unit, (Throwable) -> Unit>> = ArrayList()

    fun download(urlStr: String, cache: Boolean, autoDisposable: AutoDisposable, error: (Throwable) -> Unit, success: (Bitmap) -> Unit) {
        if (!urlStr.matches(URL_REGEX)) {
            throw IllegalArgumentException("passed urlStr is not in the form of URL")
        }
        bitmapCache[urlStr]?.let {
            success(it)
            return
        }
        if (queue.any { it.first == urlStr }) {
            queue.add(Triple(urlStr, success, error))
        } else {
            autoDisposable.singleTask(MySchedulers.imageLoad) {
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

fun ImageView.download(url: String, autoDisposable: AutoDisposable, cache: Boolean = true) {
    this.setImageDrawable(null)
    ImageCache.download(url, cache, autoDisposable, {
        this.setImageDrawable(null)
    }, {
        this.setImageBitmap(it)
    })
}