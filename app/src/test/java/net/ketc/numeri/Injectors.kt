package net.ketc.numeri

import net.ketc.numeri.domain.*
import net.ketc.numeri.domain.service.OAuthServiceSpecDependency


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

    val testDomainComponent: TestDomainComponent  by lazy {
        DaggerTestDomainComponent.builder().testDomainModule(TestDomainModule()).build()
    }
}