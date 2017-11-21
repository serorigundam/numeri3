package tech.ketc.numeri.ui.model.di

import android.arch.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import tech.ketc.numeri.ui.activity.main.MainActivityComponent
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivityComponent
import tech.ketc.numeri.ui.fragment.main.MainFragmentComponent
import tech.ketc.numeri.ui.fragment.timeline.TimeLineFragmentComponent
import tech.ketc.numeri.ui.fragment.timelinegroup.TimelineGroupManageFragmentComponent
import tech.ketc.numeri.ui.fragment.timelinemanage.TimelineManageFragmentComponent
import tech.ketc.numeri.ui.model.factory.ViewModelFactory
import javax.inject.Singleton


@Module(subcomponents = arrayOf(
        MainActivityComponent::class,
        MainFragmentComponent::class,
        TimeLineFragmentComponent::class,
        TimelineManageActivityComponent::class,
        TimelineGroupManageFragmentComponent::class,
        TimelineManageFragmentComponent::class))
class ModelFactoryModule {

    @Singleton
    @Provides
    fun provideViewModelFactory(builder: ViewModelComponent.Builder): ViewModelProvider.Factory = ViewModelFactory(builder.build())
}