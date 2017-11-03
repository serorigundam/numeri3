package net.ketc.numeri.util.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.webkit.MimeTypeMap
import net.ketc.numeri.util.android.save
import java.io.File


class ImageResizer(private val ctx: Context, private val imageFile: File, private val sizeLimit: Int) {

    val result: File
        get() = resizedImageFile ?: throw IllegalStateException()
    private var resizedImageFile: File? = null

    fun resize(): Boolean {
        //todo 試験的
        val extension = imageFile.extension.toLowerCase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        val size = imageFile.readBytes().size
        Log.v(javaClass.name, "extension = ${imageFile.extension}, mimeType = $mimeType,size = $size")
        if (sizeLimit >= size) return false
        val absolutePath = imageFile.absolutePath
        val bitmap = BitmapFactory.decodeFile(absolutePath)
        val width = bitmap.width
        val height = bitmap.height
        Log.v(javaClass.name, "$width * $height")
        val scale = (sizeLimit / size.toFloat())
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        Log.v(javaClass.name, "$newWidth * $newHeight")

        val resized = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
        resizedImageFile = resized.save(ctx, mimeType, (scale * 100).toInt())
        bitmap.recycle()
        return true
    }

}