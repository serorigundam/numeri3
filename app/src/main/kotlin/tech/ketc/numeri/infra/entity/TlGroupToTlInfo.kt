package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import java.io.Serializable

@Entity(tableName = "timeline_group_to_timeline_info",
        foreignKeys = [ForeignKey(entity = TimelineGroup::class, parentColumns = ["name"], childColumns = ["group_name"]),
        ForeignKey(entity = TimelineInfo::class, parentColumns = ["id"], childColumns = ["info_id"])],
        indices = [Index("group_name", name = "group_name_index"), Index("info_id", name = "info_id_index")],
        primaryKeys = ["group_name", "info_id"])
class TlGroupToTlInfo(@ColumnInfo(name = "group_name")
                      val groupName: String,
                      @ColumnInfo(name = "info_id")
                      val infoId: Int,
                      @ColumnInfo(name = "order_num")
                      var order: Int) : Serializable