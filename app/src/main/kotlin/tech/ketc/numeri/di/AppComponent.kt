package tech.ketc.numeri.di

import dagger.Component
import tech.ketc.numeri.App
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import tech.ketc.numeri.domain.repository.di.RepositoryModule
import tech.ketc.numeri.domain.twitter.di.TwitterModule
import tech.ketc.numeri.infra.di.InfraModule
import tech.ketc.numeri.ui.activity.di.ActivityModule
import tech.ketc.numeri.ui.fragment.di.FragmentModule
import tech.ketc.numeri.ui.model.di.ModelFactoryModule
import javax.inject.Singleton


@Singleton
@Component(modules = [AndroidSupportInjectionModule::class, ActivityModule::class, FragmentModule::class,
ModelFactoryModule::class, RepositoryModule::class, InfraModule::class, TwitterModule::class])
interface AppComponent : AndroidInjector<App> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}