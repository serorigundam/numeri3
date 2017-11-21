package tech.ketc.numeri.ui.fragment.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.ketc.numeri.ui.fragment.main.MainFragment
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragment
import tech.ketc.numeri.ui.fragment.timelinemanage.TimelineManageFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeTimelineFragment(): TimelineFragment

    @ContributesAndroidInjector
    abstract fun contributeTimelinGroupManageFragment(): TimelineGroupManageFragment

    @ContributesAndroidInjector
    abstract fun contributeTimelinManageFragment(): TimelineManageFragment
}