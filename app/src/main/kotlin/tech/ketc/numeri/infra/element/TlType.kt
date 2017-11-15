package tech.ketc.numeri.infra.element

import android.arch.persistence.room.TypeConverter
import java.io.Serializable

enum class TlType : Serializable {
    HOME, MENTIONS, USER_LIST, PUBLIC, FAVORITE;

    class Converter {
        @TypeConverter
        fun fromString(name: String) = TlType.valueOf(name)

        @TypeConverter
        fun tlTypeToString(tlType: TlType) = tlType.name
    }
}