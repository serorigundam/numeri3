package net.ketc.numeri

import android.content.Context
import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.util.twitter.TwitterApp
import net.ketc.numeri.util.ormlite.DataBaseHelper
import net.ketc.numeri.util.twitter.OAuthSupportFactory
import twitter4j.auth.OAuthSupport
import javax.inject.Singleton

@Module
class AppModule() {

    @Provides
    @Singleton
    fun provideApplicationContext(): Context = DummyContext()

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

@Singleton
@Component(modules = arrayOf(AppModule::class, TwitterAppModule::class))
interface AppComponent {
    fun inject(databaseHelper: DataBaseHelper)
    fun inject(oAuthServiceImpl: OAuthServiceImpl)
}

fun DataBaseHelper.inject() {
    Injectors.appComponent.inject(this)
}

fun OAuthServiceImpl.inject() {
    Injectors.appComponent.inject(this)
}