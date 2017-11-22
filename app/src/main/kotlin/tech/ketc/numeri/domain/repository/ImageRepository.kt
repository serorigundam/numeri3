package tech.ketc.numeri.domain.repository

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.LruCache
import android.webkit.MimeTypeMap
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
import tech.ketc.numeri.infra.element.MimeType
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


class ImageRepository @Inject constructor(private val mApp: App, private val mDatabase: ImageDatabase) : IImageRepository {

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

    private fun MimeType.toFormat() = when (this) {
        MimeType.PNG -> Bitmap.CompressFormat.PNG
        MimeType.JPEG -> Bitmap.CompressFormat.JPEG
    }

    private fun mimeType(str: String) = when (str) {
        MimeType.JPEG.toString() -> MimeType.JPEG
        MimeType.PNG.toString() -> MimeType.PNG
        else -> MimeType.PNG
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


    override fun save(bitmap: Bitmap, mimeType: MimeType, directory: String, fileName: String, quality: Int): File {
        val file = File("${Environment.getExternalStorageDirectory().absolutePath}/$directory")
        if (!file.exists() && !file.mkdir()) throw IOException("directory creation failure")

        val mimeTypeStr = mimeType.toString()
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeTypeStr)
        val path = "${file.absolutePath}/$fileName.$extension"
        FileOutputStream(path).use { stream ->
            bitmap.compress(mimeType.toFormat(), quality, stream)
        }
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, fileName)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.MIME_TYPE, mimeTypeStr)
            put(MediaStore.Images.Media.DATA, path)
        }
        mApp.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return File(path)
    }
}