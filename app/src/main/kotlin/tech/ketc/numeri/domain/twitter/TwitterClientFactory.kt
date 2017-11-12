package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.infra.entity.AccountToken
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import javax.inject.Inject

class TwitterClientFactory @Inject constructor(private val app: App) : ITwitterClientFactory {
    override fun create(token: AccountToken): TwitterClient = TwitterClientInternal(app, token)

    class TwitterClientInternal(app: App, token: AccountToken) : TwitterClient {
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
}