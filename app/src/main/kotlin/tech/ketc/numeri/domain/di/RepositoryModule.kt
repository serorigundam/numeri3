package tech.ketc.numeri.domain.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.domain.AccountRepository
import tech.ketc.numeri.domain.IAccountRepository
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.ui.model.di.ViewModelComponent
import javax.inject.Singleton

@Module(subcomponents = arrayOf(ViewModelComponent::class))
class RepositoryModule {
    @Singleton
    @Provides
    fun provideAccountRepository(db: AccountDatabase): IAccountRepository = AccountRepository(db)
}