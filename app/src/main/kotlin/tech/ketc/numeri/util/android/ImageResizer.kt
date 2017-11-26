package tech.ketc.numeri.util.android

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.MimeTypeMap
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import java.io.File
import tech.ketc.numeri.infra.element.mimeType
import java.util.*

class ImageResizer(private val ctx: Context, private val imageFile: File, private val sizeLimit: Int) {

    val result: File
        get() = resizedImageFile ?: throw IllegalStateException()
    private var resizedImageFile: File? = null

    fun resize(): Boolean {
        //todo 試験的
        val extension = imageFile.extension.toLowerCase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val size = imageFile.readBytes().size
        Logger.v(logTag, "extension = ${imageFile.extension}, mimeType = $mimeType,size = $size")
        if (sizeLimit >= size) return false

        val absolutePath = imageFile.absolutePath
        val options = BitmapFactory.Options()
        options.inScaled = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(absolutePath, options)
        val width = bitmap.width
        val height = bitmap.height
        Logger.v(javaClass.name, "$width * $height")
        val scale = (sizeLimit / size.toFloat())
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        Logger.v(javaClass.name, "$newWidth * $newHeight")
        Logger.v(logTag, "scale $scale")
        val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
        resizedImageFile = resized.save(ctx, mimeType(mimeType), "numetter/resize", Date().time.toString(), (100 * scale).toInt())
        bitmap.recycle()
        return true
    }
}