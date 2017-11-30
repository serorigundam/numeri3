package tech.ketc.numeri.ui.activity.di

import android.app.Activity
import dagger.android.AndroidInjector
import tech.ketc.numeri.ui.activity.main.MainActivity
import dagger.android.ActivityKey
import dagger.multibindings.IntoMap
import dagger.Binds
import dagger.Module
import tech.ketc.numeri.ui.activity.conversation.ConversationActivity
import tech.ketc.numeri.ui.activity.conversation.ConversationActivityComponent
import tech.ketc.numeri.ui.activity.main.MainActivityComponent
import tech.ketc.numeri.ui.activity.media.MediaActivity
import tech.ketc.numeri.ui.activity.media.MediaActivityComponent
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivity
import tech.ketc.numeri.ui.activity.timelinemanage.TimelineManageActivityComponent
import tech.ketc.numeri.ui.activity.tweet.TweetActivity
import tech.ketc.numeri.ui.activity.tweet.TweetActivityComponent
import tech.ketc.numeri.ui.activity.user.UserInfoActivity
import tech.ketc.numeri.ui.activity.user.UserInfoActivityComponent


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

    @Binds
    @IntoMap
    @ActivityKey(MediaActivity::class)
    internal abstract fun bindInjectorFactoryForMediaActivity(builder: MediaActivityComponent.Builder): AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(ConversationActivity::class)
    internal abstract fun bindInjectorFactoryForConversationActivity(builder: ConversationActivityComponent.Builder): AndroidInjector.Factory<out Activity>

    @Binds
    @IntoMap
    @ActivityKey(UserInfoActivity::class)
    internal abstract fun bindInjectorFactoryForUserInfoActivity(builder: UserInfoActivityComponent.Builder): AndroidInjector.Factory<out Activity>
}