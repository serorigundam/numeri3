package tech.ketc.numeri.domain.model

import android.graphics.Bitmap
import tech.ketc.numeri.infra.element.MimeType

data class BitmapContent(val bitmap: Bitmap, val mimeType: MimeType)