package tech.ketc.numeri.infra.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "timeline_group")
class TimelineGroup(@PrimaryKey
                    @ColumnInfo(name = "name")
                    val name: String)