package net.ketc.numeri

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.util.twitter.TwitterApp
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.util.ormlite.DataBaseHelper
import net.ketc.numeri.util.twitter.OAuthSupportFactory
import net.ketc.numeri.util.twitter.OAuthSupportFactoryImpl
import net.ketc.numeri.util.twitter.TwitterAppImpl
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideTwitterApp(): TwitterApp = TwitterAppImpl(Numeri.application)

}

@Module
class TwitterAppModule() {

    @Provides
    @Singleton
    fun provideOAuthSupportFactory(): OAuthSupportFactory = OAuthSupportFactoryImpl()
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