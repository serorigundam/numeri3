package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.infra.entity.AccountToken
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder
import javax.inject.Inject

class TwitterClientFactory @Inject constructor(private val mApp: App) : ITwitterClientFactory {
    override fun create(token: AccountToken): TwitterClient = TwitterClientInternal(mApp, token)

    class TwitterClientInternal(app: App, token: AccountToken) : TwitterClient {
        override val id: Long = token.id
        override val twitter: Twitter

        init {
            Logger.v(logTag, "new instance")
            val configuration = ConfigurationBuilder()
                    .setOAuthConsumerKey(app.twitterApiKey)
                    .setOAuthConsumerSecret(app.twitterSecretKey)
                    .setOAuthAccessToken(token.authToken)
                    .setOAuthAccessTokenSecret(token.authTokenSecret)
                    .setTweetModeExtended(true)
                    .build()
            twitter = TwitterFactory(configuration).instance
        }

        override fun equals(other: Any?): Boolean {
            if (other !is TwitterClient) return false
            return id == other.id
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }
}