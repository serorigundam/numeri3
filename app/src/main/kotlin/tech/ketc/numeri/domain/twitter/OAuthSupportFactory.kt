package tech.ketc.numeri.domain.twitter

import tech.ketc.numeri.App
import twitter4j.auth.OAuthAuthorization
import twitter4j.conf.ConfigurationContext
import javax.inject.Inject

class OAuthSupportFactory @Inject constructor(private val app: App) : IOAuthSupportFactory {
    override fun create() = OAuthAuthorization(ConfigurationContext.getInstance())
            .apply { setOAuthConsumer(app.twitterApiKey, app.twitterSecretKey) }
}