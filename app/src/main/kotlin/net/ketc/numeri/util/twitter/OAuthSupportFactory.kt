package net.ketc.numeri.util.twitter

import twitter4j.auth.OAuthAuthorization
import twitter4j.auth.OAuthSupport
import twitter4j.conf.ConfigurationContext

interface OAuthSupportFactory {
    fun create(): OAuthSupport
}

class OAuthSupportFactoryImpl : OAuthSupportFactory {
    private val configurationContext = ConfigurationContext.getInstance()
    override fun create() = OAuthAuthorization(configurationContext)
}