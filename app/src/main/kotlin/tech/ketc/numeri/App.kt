package tech.ketc.numeri

import android.annotation.SuppressLint
import android.util.Log
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric
import tech.ketc.numeri.di.DaggerAppComponent
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.di.applyAutoInject
import tech.ketc.numeri.util.logTag


class App : DaggerApplication() {

    @SuppressLint("CheckResult")
    override fun onCreate() {
        val debug = resources.getBoolean(R.bool.debug)
        if (debug) {
            debugMode()
        } else {
            Fabric.with(this, Crashlytics())
        }
        applyAutoInject()
        super.onCreate()
    }

    private fun debugMode() {
        Logger.debug = true
        Log.v(javaClass.name, "mode:debug")
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.v(logTag, "Uncaught", throwable)
            defaultUncaughtExceptionHandler.uncaughtException(thread, throwable)
        }
    }


    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().create(this)
}