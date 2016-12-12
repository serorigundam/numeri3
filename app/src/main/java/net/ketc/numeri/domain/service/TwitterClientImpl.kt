package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.util.twitter.TwitterApp
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder


interface TwitterClient {
    val twitter: Twitter
    val id: Long

}


class TwitterClientImpl(twitterApp: TwitterApp, token: ClientToken) : TwitterClient {
    override val twitter: Twitter
    override val id = token.id

    init {
        val configuration = ConfigurationBuilder()
                .setOAuthConsumerKey(twitterApp.apiKey)
                .setOAuthConsumerSecret(twitterApp.apiSecret)
                .setOAuthAccessToken(token.authToken)
                .setOAuthAccessTokenSecret(token.authTokenSecret)
                .build()
        twitter = TwitterFactory(configuration).instance
    }
}