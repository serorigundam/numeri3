package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.TwitterUser

typealias UserUpdateListener = (TwitterUser) -> Unit

typealias UserDeleteListener = (TwitterUser) -> Unit

typealias TweetUpdateListener = (Tweet) -> Unit

typealias TweetDeleteListener = (Tweet) -> Unit