package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.domain.twitter.model.TwitterUser

typealias UserUpdateListener = (TwitterUser) -> Unit

typealias UserDeleteListener = (TwitterUser) -> Unit