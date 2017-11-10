package tech.ketc.numeri.infra.element

import android.arch.persistence.room.TypeConverter


enum class MimeType(val typeStr: String) {
    JPEG("image/png"), PNG("image/jpeg");

    override fun toString(): String {
        return typeStr
    }

    companion object {
        fun from(mimeType: String) = MimeType.values().find { it.toString() == mimeType } ?: throw IllegalArgumentException()
    }

    class Converter {
        @TypeConverter
        fun fromString(mimeType: String) = when (mimeType) {
            MimeType.JPEG.toString() -> MimeType.JPEG
            MimeType.PNG.toString() -> MimeType.PNG
            else -> throw IllegalArgumentException()
        }

        @TypeConverter
        fun mimeTypeToString(mimeType: MimeType) = mimeType.toString()
    }
}