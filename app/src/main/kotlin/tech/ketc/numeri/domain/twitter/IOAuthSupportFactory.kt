package tech.ketc.numeri.domain.twitter

import twitter4j.auth.OAuthSupport

interface IOAuthSupportFactory {
    fun create(): OAuthSupport
}