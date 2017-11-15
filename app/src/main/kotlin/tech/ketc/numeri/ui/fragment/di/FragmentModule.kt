package tech.ketc.numeri.ui.fragment.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.ketc.numeri.ui.fragment.main.MainFragment
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributeTimelineFragment(): TimelineFragment

}