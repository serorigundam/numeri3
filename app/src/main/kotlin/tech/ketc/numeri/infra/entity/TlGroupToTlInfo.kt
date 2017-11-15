package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import java.io.Serializable

@Entity(tableName = "timeline_group_to_timeline_info", foreignKeys = arrayOf(
        ForeignKey(entity = TimelineGroup::class, parentColumns = arrayOf("name"), childColumns = arrayOf("group_name")),
        ForeignKey(entity = TimelineInfo::class, parentColumns = arrayOf("id"), childColumns = arrayOf("info_id"))),
        indices = arrayOf(
                Index(value = "group_name", name = "group_name_index"),
                Index(value = "info_id", name = "info_id_index")),
        primaryKeys = arrayOf("group_name", "info_id"))
class TlGroupToTlInfo(@ColumnInfo(name = "group_name")
                      val groupName: String,
                      @ColumnInfo(name = "info_id")
                      val infoId: Int,
                      @ColumnInfo(name = "order_num")
                      var order: Int):Serializable