package tech.ketc.numeri.domain.twitter.client

import android.arch.lifecycle.LiveData
import tech.ketc.numeri.domain.twitter.model.Tweet
import twitter4j.StatusDeletionNotice

interface TwitterStream {
    val latestTweet: LiveData<Tweet>
    val latestTweetDeletionNotice: LiveData<StatusDeletionNotice>
    val latestFavoriteNotice: LiveData<StatusNotice>
    val latestUnFavoriteNotice: LiveData<StatusNotice>
    val latestFollowNotice: LiveData<UserNotice>
    val latestUnFollowNotice: LiveData<UserNotice>
    val latestBlockNotice: LiveData<UserNotice>
    val latestUnBlockNotice: LiveData<UserNotice>
}