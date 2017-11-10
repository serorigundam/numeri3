package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.*
import tech.ketc.numeri.infra.element.MimeType
import java.util.*

@Entity(tableName = "image")
class Image(@PrimaryKey
            @ColumnInfo(name = "id")
            val id: String,
            @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
            val pngData: ByteArray,
            @ColumnInfo(name = "mime_type")
            val mimeType: MimeType,
            @ColumnInfo(name = "timestamp")
            val timestamp: Long = Date().time)


