package tech.ketc.numeri.ui.activity.di

import android.app.Activity
import dagger.android.AndroidInjector
import tech.ketc.numeri.ui.activity.main.MainActivity
import dagger.android.ActivityKey
import dagger.multibindings.IntoMap
import dagger.Binds
import dagger.Module
import tech.ketc.numeri.ui.activity.main.MainActivityComponent
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivity
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivityComponent
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.ui.activity.tweet.TweetActivityComponent


@Module
abstract class ActivityModule {

    @Binds
    @IntoMap
    @ActivityKey(MainActivity::class)
    internal abstract fun bindInjectorFactoryForMain(builder: MainActivityComponent.Builder): AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(TweetActivity::class)
    internal abstract fun bindInjectorFactoryForTweetActivity(builder: TweetActivityComponent.Builder): AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(TimelineManageActivity::class)
    internal abstract fun bindInjectorFactoryForTimalinaManage(builder: TimelineManageActivityComponent.Builder): AndroidInjector.Factory<out Activity>
}