package tech.ketc.numeri.domain.repository

import android.graphics.BitmapFactory
import android.util.LruCache
import tech.ketc.numeri.App
import tech.ketc.numeri.infra.ImageDatabase
import tech.ketc.numeri.infra.entity.Image
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.infra.element.mimeType
import tech.ketc.numeri.infra.element.toFormat
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ImageRepository @Inject constructor(private val mDatabase: ImageDatabase) : IImageRepository {

    companion object {
        private val MAX = 5 * 1024 * 1024
        private val LOCAL_RECORD_MAX = 150
    }

    private val mDao = mDatabase.imageDao()

    private val mDownloadLock = LinkedHashMap<String, ReentrantLock>()
    private val mCacheWriteLock = LinkedHashMap<String, ReentrantLock>()
    private val mDatabaseLock = LinkedHashMap<String, ReentrantLock>()

    private val bitmapCache = object : LruCache<String, BitmapContent>(MAX) {
        override fun sizeOf(key: String, value: BitmapContent): Int {
            return value.bitmap.byteCount
        }
    }

    private fun <R> downloadLock(urlStr: String, handle: () -> R): R {
        val lock = mDownloadLock[urlStr]
        lock ?: return handle()
        return lock.withLock(handle)
    }

    private fun <R> cacheWriteLock(urlStr: String, handle: () -> R): R {
        val lock = mCacheWriteLock[urlStr]
        lock ?: return handle()
        return lock.withLock(handle)
    }

    private fun <R> databaseLock(urlStr: String, handle: () -> R): R {
        val lock = mDatabaseLock[urlStr]
        lock ?: return handle()
        return lock.withLock(handle)
    }


    override fun downloadOrGet(urlStr: String, cache: Boolean) = downloadLock(urlStr) {
        localGet(urlStr) ?: download(urlStr, cache)
    }

    private fun localGet(urlStr: String): BitmapContent? {
        fun getForDB(): BitmapContent? {
            val image = mDao.selectById(urlStr)
                    ?: return null
            val data = image.pngData
            val mimeType = image.mimeType
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            return BitmapContent(bitmap, mimeType).also {
                cache(urlStr, it)
            }
        }
        return cacheWriteLock(urlStr) { bitmapCache[urlStr] } ?: databaseLock(urlStr) { getForDB() }
    }

    private fun cache(urlStr: String, content: BitmapContent) {
        mCacheWriteLock.put(urlStr, ReentrantLock())
        bitmapCache.put(urlStr, content)
        Logger.v(logTag, "cache $urlStr")
        mCacheWriteLock.remove(urlStr)
    }

    private fun download(urlStr: String, cache: Boolean): BitmapContent {
        mDownloadLock.put(urlStr, ReentrantLock())
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        return try {
            val url = URL(urlStr)
            connection = url.openConnection()!! as HttpURLConnection
            connection.allowUserInteraction = false
            connection.instanceFollowRedirects = false
            connection.requestMethod = "GET"
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.inputStream
                val contentType = connection.contentType.toLowerCase()
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val mimeType = mimeType(contentType)
                Logger.v(logTag, "download $urlStr")
                BitmapContent(bitmap, mimeType).also {
                    if (!cache) return@also
                    saveLocalDatabase(urlStr, it)
                }
            } else {
                throw RuntimeException("failed load image : responseCode = $responseCode")
            }
        } finally {
            inputStream?.close()
            connection?.disconnect()
            mDownloadLock.remove(urlStr)
        }
    }

    private fun saveLocalDatabase(urlStr: String, content: BitmapContent) {
        cache(urlStr, content)
        launch {
            mDatabaseLock.put(urlStr, ReentrantLock())
            Logger.v(logTag, "save local db $urlStr")
            val byteArray = async(coroutineContext + CommonPool) {
                ByteArrayOutputStream().use {
                    content.bitmap.compress(content.mimeType.toFormat(), 50, it)
                    it.toByteArray()
                }
            }.await()
            if (mDao.count() == LOCAL_RECORD_MAX) mDao.deleteOldest()
            mDao.insert(Image(urlStr, byteArray, content.mimeType))
            mDatabaseLock.remove(urlStr)
        }
    }
}