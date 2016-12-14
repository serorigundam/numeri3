package net.ketc.numeri

import dagger.Module
import dagger.Provides
import net.ketc.numeri.util.twitter.TwitterApp
import net.ketc.numeri.util.twitter.OAuthSupportFactory
import twitter4j.auth.OAuthSupport
import javax.inject.Singleton

@Module
class AppModule() {

    @Provides
    @Singleton
    fun provideTwitterApp(): TwitterApp = object : TwitterApp {
        override val apiSecret: String = "secret"
        override val apiKey: String = "key"
        override val callbackUrl: String = "callback"
    }
}

@Module
class TwitterAppModule() {

    @Provides
    @Singleton
    fun provideOAuthSupportFactory(): OAuthSupportFactory = object : OAuthSupportFactory {
        override fun create(): OAuthSupport = DummyOAuthSupport()
    }
}