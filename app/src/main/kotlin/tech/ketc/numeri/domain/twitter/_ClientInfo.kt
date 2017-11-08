package tech.ketc.numeri.domain.twitter

import android.content.Context
import tech.ketc.numeri.R

val Context.twitterApiKey: String
    get() = getString(R.string.twitter_api_key)

val Context.twitterSecretKey: String
    get() = getString(R.string.twitter_secret_key)

val Context.twitterCallbackUrl: String
    get() {
        val scheme = getString(R.string.twitter_callback_scheme)
        val host = getString(R.string.twitter_callback_host)
        return "$scheme://$host"
    }