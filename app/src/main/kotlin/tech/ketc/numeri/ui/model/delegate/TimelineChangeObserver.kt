package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.repository.ITimelineInfoRepository

class TimelineChangeObserver(private val mRepository: ITimelineInfoRepository) : ITimelineChangeObserver {
    override fun timelineChange(owner: LifecycleOwner, handle: () -> Unit) = mRepository.observe(owner, handle)
}