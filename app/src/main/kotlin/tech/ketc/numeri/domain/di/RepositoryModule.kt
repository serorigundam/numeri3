package tech.ketc.numeri.domain.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.AccountRepository
import tech.ketc.numeri.domain.IAccountRepository
import tech.ketc.numeri.domain.twitter.IOAuthSupportFactory
import tech.ketc.numeri.domain.twitter.ITwitterClientFactory
import tech.ketc.numeri.infra.AccountDatabase
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
}