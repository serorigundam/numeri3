package tech.ketc.numeri.domain.repository

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import java.io.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write


class ImageRepository @Inject constructor(private val app: App, private val db: ImageDatabase) : IImageRepository {

    companion object {
        private val MAX = 5 * 1024 * 1024
        private val LOCAL_RECORD_MAX = 150
    }

    private val dao = db.imageDao()

    private val cacheLock = ReentrantReadWriteLock()
    private val dbLock = ReentrantReadWriteLock()


    private val bitmapCache = object : LruCache<String, BitmapContent>(MAX) {
        override fun sizeOf(key: String, value: BitmapContent): Int {
            return value.bitmap.byteCount
        }
    }

    override fun downloadOrGet(urlStr: String, cache: Boolean)
            = localGet(urlStr) ?: download(urlStr, cache)

    private fun localGet(urlStr: String): BitmapContent? {
        fun getForDB(): BitmapContent? {
            val image = dbLock.read {
                dao.selectById(urlStr)
            } ?: return null
            val data = image.pngData
            val mimeType = image.mimeType
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            return BitmapContent(bitmap, mimeType).also {
                cacheLock.write { bitmapCache.put(urlStr, it) }
            }
        }
        return cacheLock.read { bitmapCache[urlStr] } ?: getForDB()
    }

    private fun download(urlStr: String, cache: Boolean): BitmapContent {
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
                Log.v(javaClass.name, "dawnload contentType=$contentType,typeStr=$mimeType")
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
        cacheLock.write { bitmapCache.put(urlStr, content) }
        launch {
            val byteArray = async(coroutineContext + CommonPool) {
                ByteArrayOutputStream().use {
                    content.bitmap.compress(content.mimeType.toFormat(), 50, it)
                    it.toByteArray()
                }
            }.await()

            dbLock.write {
                if (dao.count() == LOCAL_RECORD_MAX) dao.deleteOldest()
                dao.insert(Image(urlStr, byteArray, content.mimeType))
            }
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
        app.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        return File(path)
    }
}