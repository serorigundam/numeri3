package tech.ketc.numeri.domain.twitter.client

import twitter4j.Twitter

interface ITwitterClient {
    val id: Long
    val twitter:Twitter
}