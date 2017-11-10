package tech.ketc.numeri.domain.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.IOAuthSupportFactory
import tech.ketc.numeri.domain.twitter.ITwitterClientFactory
import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.infra.ImageDatabase
import tech.ketc.numeri.ui.model.di.ViewModelComponent
import javax.inject.Singleton

@Module(subcomponents = arrayOf(ViewModelComponent::class))
class RepositoryModule {
    @Singleton
    @Provides
    fun provideAccountRepository(app: App,
                                 db: AccountDatabase,
                                 oauthSupportFactory: IOAuthSupportFactory,
                                 twitterClientFactory: ITwitterClientFactory): IAccountRepository
            = AccountRepository(app, db, oauthSupportFactory, twitterClientFactory)

    @Singleton
    @Provides
    fun provideTwitterUserFactory(twitterUserFactory: ITwitterUserFactory): ITwitterUserRepository
            = TwitterUserRepository(twitterUserFactory)

    @Singleton
    @Provides
    fun provideImageRepository(app: App, db: ImageDatabase): IImageRepository
            = ImageRepository(app, db)

}