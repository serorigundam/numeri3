package net.ketc.numeri.util.twitter

import twitter4j.auth.AccessToken
import twitter4j.auth.OAuthSupport
import twitter4j.auth.RequestToken

class DummyOAuthSupport() : OAuthSupport {
    override fun setOAuthAccessToken(accessToken: AccessToken?) {
    }

    override fun setOAuthConsumer(consumerKey: String?, consumerSecret: String?) {
    }

    override fun getOAuthRequestToken(): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?, xAuthAccessType: String?): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?, xAuthAccessType: String?, xAuthMode: String?): RequestToken = RequestToken("", "")

    override fun getOAuthAccessToken(): AccessToken = createToken("1")

    override fun getOAuthAccessToken(oauthVerifier: String) = createToken(oauthVerifier)

    override fun getOAuthAccessToken(requestToken: RequestToken?): AccessToken = createToken("1")

    override fun getOAuthAccessToken(requestToken: RequestToken?, oauthVerifier: String?): AccessToken = createToken("1")

    override fun getOAuthAccessToken(screenName: String?, password: String?): AccessToken = createToken("1")

    private fun createToken(param: String) = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", param.toLong())
}