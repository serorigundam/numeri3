package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.App
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationContext
import javax.inject.Inject

class OAuthSupportFactory @Inject constructor(private val mApp: App) : IOAuthSupportFactory {
    override fun create() = OAuthAuthorization(ConfigurationContext.getInstance())
            .apply { setOAuthConsumer(mApp.twitterApiKey, mApp.twitterSecretKey) }
}