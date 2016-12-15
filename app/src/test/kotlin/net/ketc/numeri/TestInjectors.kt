package net.ketc.numeri

import net.ketc.numeri.domain.*


object TestInjectors {
    val testDomainComponent: TestDomainComponent  by lazy {
        DaggerTestDomainComponent.builder().testDomainModule(TestDomainModule()).build()
    }
}