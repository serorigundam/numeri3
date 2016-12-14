package net.ketc.numeri

import net.ketc.numeri.domain.DaggerDomainComponent
import net.ketc.numeri.domain.DomainComponent
import net.ketc.numeri.domain.DomainModule

object Injectors {
    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule())
                .twitterAppModule(TwitterAppModule())
                .build()
    }

    val domainComponent: DomainComponent by lazy {
        DaggerDomainComponent.builder().domainModule(DomainModule()).build()
    }
}