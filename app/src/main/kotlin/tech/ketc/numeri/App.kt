package tech.ketc.numeri

import android.util.Log
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric
import tech.ketc.numeri.di.DaggerAppComponent
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.di.applyAutoInject


class App : DaggerApplication() {

    override fun onCreate() {
        val debug = resources.getBoolean(R.bool.debug)
        if (debug) {
            Logger.debug = true
            Log.v(javaClass.name, "mode:debug")
        } else {
            Fabric.with(this, Crashlytics())
        }
        applyAutoInject()
        super.onCreate()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }
}