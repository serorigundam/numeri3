package tech.ketc.numeri.ui.fragment.di

import android.support.v4.app.Fragment
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import tech.ketc.numeri.ui.fragment.main.MainFragment
import tech.ketc.numeri.ui.fragment.main.MainFragmentComponent
import tech.ketc.numeri.ui.fragment.timeline.TimeLineFragmentComponent
import tech.ketc.numeri.ui.fragment.timeline.TimelineFragment
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragment
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragmentComponent
import tech.ketc.numeri.ui.fragment.timelinemanage.TimelineManageFragment
import tech.ketc.numeri.ui.fragment.timelinemanage.TimelineManageFragmentComponent

@Module
abstract class FragmentModule {

    @Binds
    @IntoMap
    @FragmentKey(MainFragment::class)
    abstract fun bindInjectorFactoryForMainFragment(builder: MainFragmentComponent.Builder): AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(TimelineFragment::class)
    abstract fun bindInjectorFactoryForTimelineFragment(builder: TimeLineFragmentComponent.Builder): AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(TimelineGroupManageFragment::class)
    abstract fun bindInjectorFactoryForTimelinGroupManageFragment(builder: TimelineGroupManageFragmentComponent.Builder): AndroidInjector.Factory<out Fragment>

    @Binds
    @IntoMap
    @FragmentKey(TimelineManageFragment::class)
    abstract fun bindInjectorFactoryForTimelinManageFragment(builder: TimelineManageFragmentComponent.Builder): AndroidInjector.Factory<out Fragment>
}