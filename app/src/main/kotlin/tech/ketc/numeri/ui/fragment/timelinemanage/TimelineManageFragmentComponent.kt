package tech.ketc.numeri.ui.fragment.timelinemanage

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TimelineManageFragmentComponent : AndroidInjector<TimelineManageFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TimelineManageFragment>()
}