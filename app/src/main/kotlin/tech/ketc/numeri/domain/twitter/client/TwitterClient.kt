package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.twitterApiKey
import tech.ketc.numeri.domain.twitter.twitterSecretKey
import tech.ketc.numeri.infra.entity.AccountToken
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class TwitterClient(app: App, token: AccountToken) : ITwitterClient {
    override val id: Long = token.id
    override val twitter: Twitter

    init {
        val configuration = ConfigurationBuilder()
                .setOAuthConsumerKey(app.twitterApiKey)
                .setOAuthConsumerSecret(app.twitterSecretKey)
                .setOAuthAccessToken(token.authToken)
                .setOAuthAccessTokenSecret(token.authTokenSecret)
                .setTweetModeExtended(true)
                .build()
        twitter = TwitterFactory(configuration).instance
    }
}