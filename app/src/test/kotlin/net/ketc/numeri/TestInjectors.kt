package net.ketc.numeri

import net.ketc.numeri.domain.*
import net.ketc.numeri.domain.service.OAuthServiceSpecDependency


object TestInjectors {
    val testDomainComponent: TestDomainComponent  by lazy {
        DaggerTestDomainComponent.builder().testDomainModule(TestDomainModule()).build()
    }
}