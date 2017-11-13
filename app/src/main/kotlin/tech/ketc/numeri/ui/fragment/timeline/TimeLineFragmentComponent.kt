package tech.ketc.numeri.ui.fragment.timeline

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TimeLineFragmentComponent : AndroidInjector<TimeLineFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TimeLineFragment>()
}