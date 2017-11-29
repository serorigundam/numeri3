package tech.ketc.numeri.ui.model.di

import dagger.Subcomponent
import tech.ketc.numeri.ui.model.*

@Subcomponent
interface ViewModelComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ViewModelComponent
    }

    fun mainViewModel(): MainViewModel

    fun timeLineViewModel(): TimeLineViewModel

    fun timelineManageViewModel(): TimelineManageViewModel

    fun tweetViewModel(): TweetViewModel

    fun mediaViewModel(): MediaViewModel

    fun conversationViewModel(): ConversationViewModel
}