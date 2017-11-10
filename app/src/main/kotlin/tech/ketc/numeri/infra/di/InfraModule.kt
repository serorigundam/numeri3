package tech.ketc.numeri.infra.di

import android.arch.persistence.room.Room
import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.di.RepositoryComponent
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.infra.ImageDatabase
import javax.inject.Singleton

@Module(subcomponents = arrayOf(RepositoryComponent::class))
class InfraModule {

    @Provides
    @Singleton
    fun provideAccountDatabase(app: App): AccountDatabase
            = Room.databaseBuilder(app, AccountDatabase::class.java, "account-token").build()

    @Provides
    @Singleton
    fun provideImageDatabase(app: App): ImageDatabase
            = Room.databaseBuilder(app, ImageDatabase::class.java, "image").build()
}