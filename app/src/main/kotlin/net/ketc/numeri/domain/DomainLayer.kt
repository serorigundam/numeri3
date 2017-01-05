package net.ketc.numeri.domain

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.Injectors
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.domain.service.TweetsDisplayServiceImpl
import net.ketc.numeri.presentation.presenter.MainPresenter
import javax.inject.Singleton

@Module
class DomainModule {

    @Provides
    @Singleton
    fun provideOAuthService(): OAuthService = OAuthServiceImpl()


    @Provides
    @Singleton
    fun provideTweetDisplayService(): TweetsDisplayService = TweetsDisplayServiceImpl()
}

@Singleton
@Component(modules = arrayOf(DomainModule::class))
interface DomainComponent {
    fun inject(mainPresenter: MainPresenter)
}

//extension
fun MainPresenter.inject() {
    Injectors.domainComponent.inject(this)
}