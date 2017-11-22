package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.repository.ITimelineInfoRepository
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class TimelineInfoReader(private val mRepository: ITimelineInfoRepository) : ITimelineInfoReader {
    override fun loadGroupList() = asyncResponse { mRepository.getGroupList() }
    override fun loadTimelineInfoList(groupName: String)
            = asyncResponse { mRepository.selectByGroup(TimelineGroup(groupName)) }
}