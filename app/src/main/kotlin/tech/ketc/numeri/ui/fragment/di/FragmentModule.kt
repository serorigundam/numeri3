package tech.ketc.numeri.ui.fragment.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import tech.ketc.numeri.ui.fragment.timeline.TimeLineFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeMainFragment(): TimeLineFragment
}