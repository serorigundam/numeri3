package net.ketc.numeri

import android.app.Application
import net.ketc.numeri.domain.DaggerDomainComponent
import net.ketc.numeri.domain.DomainComponent
import net.ketc.numeri.domain.DomainModule

object Injectors {
    private var mApplication: Application? = null

    fun setApplication(application: Application) {
        if (mApplication == null) {
            mApplication = application
        } else {
            throw IllegalStateException("it has already been initialized")
        }
    }

    private val application: Application
        get() {
            return mApplication ?: throw IllegalStateException("initialization has not been done")
        }

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .build()
    }

    val domainComponent: DomainComponent by lazy {
        DaggerDomainComponent.builder().domainModule(DomainModule()).build()
    }
}