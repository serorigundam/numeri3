package net.ketc.numeri

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.util.ormlite.DataBaseHelper
import net.ketc.numeri.util.twitter.*
import twitter4j.auth.OAuthSupport
import javax.inject.Singleton

@Module
open class AppModule {

    @Provides
    @Singleton
    open fun provideTwitterApp(): TwitterApp = TwitterAppImpl(Numeri.application)

}

@Module
open class TwitterAppModule {

    @Provides
    @Singleton
    open fun provideOAuthSupportFactory(): OAuthSupportFactory = OAuthSupportFactoryImpl()
}

@Singleton
@Component(modules = arrayOf(AppModule::class, TwitterAppModule::class))
interface AppComponent {
    fun inject(databaseHelper: DataBaseHelper)
    fun inject(oAuthServiceImpl: OAuthServiceImpl)
}


//extension
fun DataBaseHelper.inject() {
    Injectors.appComponent.inject(this)
}

fun OAuthServiceImpl.inject() {
    Injectors.appComponent.inject(this)
}

class TestAppModule : AppModule() {

    override fun provideTwitterApp(): TwitterApp = object : TwitterApp {
        override val apiSecret: String = "secret"
        override val apiKey: String = "key"
        override val callbackUrl: String = "callback"
    }
}

class TestTwitterAppModule : TwitterAppModule() {

    override fun provideOAuthSupportFactory(): OAuthSupportFactory = object : OAuthSupportFactory {
        override fun create(): OAuthSupport = DummyOAuthSupport()
    }
}