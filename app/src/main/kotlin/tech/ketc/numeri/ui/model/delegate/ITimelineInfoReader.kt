package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred

interface ITimelineInfoReader {
    fun loadGroupList(): ResponseDeferred<List<TimelineGroup>>
    fun loadTimelineInfoList(groupName: String): ResponseDeferred<List<TimelineInfo>>
}