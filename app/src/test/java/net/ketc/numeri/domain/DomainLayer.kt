package net.ketc.numeri.domain

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.Injectors
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.domain.service.OAuthServiceSpecDependency
import net.ketc.numeri.presentation.presenter.MainPresenter
import javax.inject.Singleton

@Module
class TestDomainModule() {

    @Provides
    @Singleton
    fun provideOAuthService(): OAuthService = OAuthServiceImpl()
}

@Singleton
@Component(modules = arrayOf(TestDomainModule::class))
interface TestDomainComponent {
    fun inject(oAuthServiceSpecDependency: OAuthServiceSpecDependency)
}


fun OAuthServiceSpecDependency.inject() {
    Injectors.testDomainComponent.inject(this)
}