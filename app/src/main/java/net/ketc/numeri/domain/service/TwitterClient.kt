package net.ketc.numeri.domain.service

import android.content.Context
import net.ketc.numeri.R
import net.ketc.numeri.domain.entity.ClientToken
import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.cache.TwitterUserCache
import twitter4j.Twitter
import twitter4j.TwitterFactory
import twitter4j.conf.ConfigurationBuilder

class TwitterClient(context: Context, token: ClientToken) {
    val twitter: Twitter
    val user: TwitterUser

    init {
        val configuration = ConfigurationBuilder()
                .setOAuthConsumerKey(context.getString(R.string.twitter_api_key))
                .setOAuthConsumerSecret(context.getString(R.string.twitter_secret_key))
                .setOAuthAccessToken(token.authToken)
                .setOAuthAccessTokenSecret(token.authTokenSecret)
                .build()
        twitter = TwitterFactory(configuration).instance
        val user = twitter.showUser(twitter.id)
        this.user = TwitterUserCache.put(user)
    }
}