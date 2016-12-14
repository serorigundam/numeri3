package net.ketc.numeri.util.twitter

import android.content.Context
import net.ketc.numeri.R


interface TwitterApp {
    val apiSecret: String
    val apiKey: String
    val callbackUrl: String
}

class TwitterAppImpl(applicationContext: Context) : TwitterApp {
    override val apiSecret: String
    override val apiKey: String
    override val callbackUrl: String

    init {
        apiKey = applicationContext.getString(R.string.twitter_api_key)
        apiSecret = applicationContext.getString(R.string.twitter_secret_key)
        val scheme = applicationContext.getString(R.string.twitter_callback_scheme)
        val host = applicationContext.getString(R.string.twitter_callback_host)
        callbackUrl = "$scheme://$host"
    }
}