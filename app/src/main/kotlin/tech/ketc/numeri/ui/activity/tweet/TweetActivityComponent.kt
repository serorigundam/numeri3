package tech.ketc.numeri.ui.activity.tweet

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface TweetActivityComponent : AndroidInjector<TweetActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<TweetActivity>()
}