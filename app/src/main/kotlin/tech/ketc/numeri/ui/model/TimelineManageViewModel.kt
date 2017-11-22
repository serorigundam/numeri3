package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.ITimelineInfoRepository
import tech.ketc.numeri.ui.model.delegate.ITimelineInfoReader
import tech.ketc.numeri.ui.model.delegate.TimelineInfoReader
import javax.inject.Inject

class TimelineManageViewModel @Inject constructor(private val mTimelineRepository: ITimelineInfoRepository)
    : ViewModel(),
        ITimelineInfoReader by TimelineInfoReader(mTimelineRepository)