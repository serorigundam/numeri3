package tech.ketc.numeri.infra.element

import android.arch.persistence.room.TypeConverter

enum class TlType {
    HOME, MENTIONS, USER, PUBLIC, FAVORITE, MEDIA;

    class Converter {
        @TypeConverter
        fun fromString(name: String) = TlType.valueOf(name)

        @TypeConverter
        fun tlTypeToString(tlType: TlType) = tlType.name
    }
}