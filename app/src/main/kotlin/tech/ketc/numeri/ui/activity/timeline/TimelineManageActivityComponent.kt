package tech.ketc.numeri.ui.activity.timeline

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TimelineManageActivityComponent : AndroidInjector<TimelineManageActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TimelineManageActivity>()
}