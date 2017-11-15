package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.repository.ITimelineInfoRepository

class TimelineChangeObserver(private val repository: ITimelineInfoRepository) : ITimelineChangeObserver {
    override fun timelineChange(owner: LifecycleOwner, handle: () -> Unit) = repository.observe(owner, handle)

}