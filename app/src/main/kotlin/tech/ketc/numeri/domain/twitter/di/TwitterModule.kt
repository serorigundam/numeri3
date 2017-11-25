package tech.ketc.numeri.domain.twitter.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.twitter.*
import javax.inject.Singleton

@Module
class TwitterModule {
    @Provides
    @Singleton
    fun provideOauthSupportFactory(app: App): IOAuthSupportFactory = OAuthSupportFactory(app)

    @Provides
    @Singleton
    fun provideTwitterClientFactory(app: App): ITwitterClientFactory = TwitterClientFactory(app)

    @Provides
    @Singleton
    fun provideTwitterUserFactory(): ITwitterUserFactory = TwitterUserFactory()

    @Provides
    @Singleton
    fun provideTweetFactory(stateFactory: ITweetStateFactory): ITweetFactory = TweetFactory(stateFactory)

    @Provides
    @Singleton
    fun provideTwitterStreamFactory(app: App): ITwitterStreamFactory = TwitterStreamFactory(app)

    @Provides
    @Singleton
    fun provideTweetStateFactory(): ITweetStateFactory = TweetStateFactory()
}