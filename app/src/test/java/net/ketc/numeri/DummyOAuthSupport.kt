package net.ketc.numeri

import twitter4j.auth.AccessToken
import twitter4j.auth.OAuthSupport
import twitter4j.auth.RequestToken
import java.util.*

class DummyOAuthSupport() : OAuthSupport {
    override fun setOAuthAccessToken(accessToken: AccessToken?) {
    }

    override fun setOAuthConsumer(consumerKey: String?, consumerSecret: String?) {
    }

    override fun getOAuthRequestToken(): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?, xAuthAccessType: String?): RequestToken = RequestToken("", "")

    override fun getOAuthRequestToken(callbackURL: String?, xAuthAccessType: String?, xAuthMode: String?): RequestToken = RequestToken("", "")

    override fun getOAuthAccessToken(): AccessToken = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", UUID.randomUUID().leastSignificantBits)

    override fun getOAuthAccessToken(oauthVerifier: String?): AccessToken = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", UUID.randomUUID().leastSignificantBits)

    override fun getOAuthAccessToken(requestToken: RequestToken?): AccessToken = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", UUID.randomUUID().leastSignificantBits)

    override fun getOAuthAccessToken(requestToken: RequestToken?, oauthVerifier: String?): AccessToken = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", UUID.randomUUID().leastSignificantBits)

    override fun getOAuthAccessToken(screenName: String?, password: String?): AccessToken = AccessToken("3259477855-6R1IyToSPersxHxsYoLJoyrNTsp79CSjobLLCfy", "EZeW8ECSYcW8KQSteiLhyxkb8iKKnIAPFz7PUDlZmlIfX", UUID.randomUUID().leastSignificantBits)

}