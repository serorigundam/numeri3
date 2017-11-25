package tech.ketc.numeri.domain.twitter.client

import twitter4j.Twitter
import java.io.Serializable

interface TwitterClient : Serializable {
    val id: Long
    val twitter: Twitter
}