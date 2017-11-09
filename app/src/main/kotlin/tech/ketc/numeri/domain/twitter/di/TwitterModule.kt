package tech.ketc.numeri.domain.twitter.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.di.RepositoryComponent
import tech.ketc.numeri.domain.twitter.*
import javax.inject.Singleton

@Module(subcomponents = arrayOf(RepositoryComponent::class))
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
}