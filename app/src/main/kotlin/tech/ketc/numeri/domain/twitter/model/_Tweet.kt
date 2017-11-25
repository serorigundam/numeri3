package tech.ketc.numeri.domain.twitter.model

val Tweet.link: String
    get() = "https://twitter.com/${user.screenName}/status/$id"