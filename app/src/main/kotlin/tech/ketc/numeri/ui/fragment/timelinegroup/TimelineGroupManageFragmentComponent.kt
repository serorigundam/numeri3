package tech.ketc.numeri.ui.fragment.timelinegroup

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TimelineGroupManageFragmentComponent : AndroidInjector<TimelineGroupManageFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TimelineGroupManageFragment>()
}