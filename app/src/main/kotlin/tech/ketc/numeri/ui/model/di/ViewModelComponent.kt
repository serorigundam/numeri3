package tech.ketc.numeri.ui.model.di

import dagger.Subcomponent
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.ui.model.TimeLineViewModel
import tech.ketc.numeri.ui.model.TimelineManageViewModel

@Subcomponent
interface ViewModelComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ViewModelComponent
    }

    fun mainViewModel(): MainViewModel

    fun timeLineViewModel(): TimeLineViewModel

    fun timelineManageViewModel(): TimelineManageViewModel
}