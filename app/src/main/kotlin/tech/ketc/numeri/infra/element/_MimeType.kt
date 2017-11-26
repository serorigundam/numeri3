package tech.ketc.numeri.infra.element

import android.graphics.Bitmap

fun MimeType.toFormat() = when (this) {
    MimeType.PNG -> Bitmap.CompressFormat.PNG
    MimeType.JPEG -> Bitmap.CompressFormat.JPEG
}

fun mimeType(str: String) = when (str) {
    MimeType.JPEG.toString() -> MimeType.JPEG
    MimeType.PNG.toString() -> MimeType.PNG
    else -> MimeType.PNG
}