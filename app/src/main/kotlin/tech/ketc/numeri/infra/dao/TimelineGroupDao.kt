package tech.ketc.numeri.infra.dao

import android.arch.persistence.room.Dao
import tech.ketc.numeri.infra.entity.TimelineGroup

@Dao
interface TimelineGroupDao : IDao<TimelineGroup>
