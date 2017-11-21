package tech.ketc.numeri.ui.activity.timelinemanage

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TimelineManageActivityComponent : AndroidInjector<TimelineManageActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TimelineManageActivity>()
}