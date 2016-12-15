package net.ketc.numeri

import net.ketc.numeri.domain.DaggerDomainComponent
import net.ketc.numeri.domain.DomainComponent
import net.ketc.numeri.domain.DomainModule

object Injectors {

    var test = true

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(if (test) TestAppModule() else AppModule())
                .twitterAppModule(if (test) TestTwitterAppModule() else TwitterAppModule())
                .build()
    }

    val domainComponent: DomainComponent by lazy {
        DaggerDomainComponent.builder().domainModule(DomainModule()).build()
    }
}