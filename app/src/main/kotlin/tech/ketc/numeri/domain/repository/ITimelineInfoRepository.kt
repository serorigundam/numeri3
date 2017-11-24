package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo

interface ITimelineInfoRepository {

    fun observe(owner: LifecycleOwner, handle: () -> Unit)

    fun createGroup(groupName: String): TimelineGroup

    fun joinToGroup(group: TimelineGroup, info: TimelineInfo)

    fun removeFromGroup(group: TimelineGroup, info: TimelineInfo)

    fun selectByGroup(group: TimelineGroup): List<TimelineInfo>

    fun getInfo(type: TlType, accountId: Long, foreignId: Long = -1): TimelineInfo

    fun getGroupList(): List<TimelineGroup>

    fun deleteGroup(vararg group: TimelineGroup)

    fun notifyDataChanged()

    fun replace(group: TimelineGroup, from: TimelineInfo, to: TimelineInfo)

    fun insert(group: TimelineGroup, info: TimelineInfo, order: Int)
}