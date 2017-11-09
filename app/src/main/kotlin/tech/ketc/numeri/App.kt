package tech.ketc.numeri

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import tech.ketc.numeri.di.DaggerAppComponent
import tech.ketc.numeri.util.di.applyAutoInject


class App : DaggerApplication() {

    override fun onCreate() {
        super.onCreate()
        applyAutoInject()
        //todo Fabric.with(this, Crashlytics())
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }


}