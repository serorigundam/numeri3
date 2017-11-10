package tech.ketc.numeri.ui.model.di

import android.arch.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import tech.ketc.numeri.ui.activity.main.MainActivityComponent
import tech.ketc.numeri.ui.model.factory.ViewModelFactory
import javax.inject.Singleton


@Module(subcomponents = arrayOf(MainActivityComponent::class))
class ModelFactoryModule {

    @Singleton
    @Provides
    fun provideViewModelFactory(builder: ViewModelComponent.Builder): ViewModelProvider.Factory = ViewModelFactory(builder.build())
}