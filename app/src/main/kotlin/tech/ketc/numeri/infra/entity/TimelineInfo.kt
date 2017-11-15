package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.*
import tech.ketc.numeri.infra.element.TlType
import java.io.Serializable

@Entity(tableName = "timeline_info",
        foreignKeys = arrayOf(ForeignKey(entity = AccountToken::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("account_id"))),
        indices = arrayOf(Index("type", "account_id", "foreign_id", unique = true),
                Index(value = "account_id", name = "account_id_index")))
class TimelineInfo(@PrimaryKey(autoGenerate = true)
                   @ColumnInfo(name = "id")
                   val id: Int = 0,
                   @ColumnInfo(name = "type")
                   val type: TlType,
                   @ColumnInfo(name = "account_id")
                   val accountId: Long,
                   @ColumnInfo(name = "foreign_id")
                   val foreignId: Long = -1):Serializable