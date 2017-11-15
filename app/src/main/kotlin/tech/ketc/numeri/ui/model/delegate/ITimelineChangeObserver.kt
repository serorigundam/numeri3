package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner

interface ITimelineChangeObserver {
    fun timelineChange(owner: LifecycleOwner, handle: () -> Unit)
}