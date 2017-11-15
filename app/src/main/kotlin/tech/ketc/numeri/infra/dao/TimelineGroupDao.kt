package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import tech.ketc.numeri.infra.entity.TimelineGroup

@Dao
interface TimelineGroupDao : IDao<TimelineGroup> {
    @Query("SELECT * FROM timeline_group")
    fun selectAll(): List<TimelineGroup>
}