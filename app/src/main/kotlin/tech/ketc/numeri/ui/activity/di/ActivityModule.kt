package tech.ketc.numeri.ui.activity.di

import android.app.Activity
import dagger.android.AndroidInjector
import tech.ketc.numeri.ui.activity.main.MainActivity
import dagger.android.ActivityKey
import dagger.multibindings.IntoMap
import dagger.Binds
import dagger.Module
import tech.ketc.numeri.ui.activity.main.MainActivityComponent
import tech.ketc.numeri.ui.activity.timeline.TimelineManageActivity
import tech.ketc.numeri.ui.activity.timeline.TimelineManageActivityComponent


@Module
abstract class ActivityModule {

    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    internal abstract fun bindInjectorFactoryForMain(builder: MainActivityComponent.Builder): AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(TimelineManageActivity::class)
    internal abstract fun bindInjectorFactoryForTimalinaManage(builder: TimelineManageActivityComponent.Builder): AndroidInjector.Factory<out Activity>
}