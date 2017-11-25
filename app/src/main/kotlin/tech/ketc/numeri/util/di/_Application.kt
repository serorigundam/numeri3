package tech.ketc.numeri.util.di

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag

fun Application.applyAutoInject()
        = registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
    @SuppressLint("CheckResult")
    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        Logger.v(activity.logTag, "onCrate() isRestore:${p1 != null}")
        if (activity is AutoInject || activity is HasSupportFragmentInjector) {
            AndroidInjection.inject(activity)
        }
        if (activity !is FragmentActivity) return

        activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, s: Bundle?) {
                        if (f is AutoInject) {
                            AndroidSupportInjection.inject(f)
                        }
                    }
                }, true)
    }

    //not use
    override fun onActivityPaused(activity: Activity) {
        Logger.v(activity.logTag, "onPause()")
    }

    override fun onActivityResumed(activity: Activity) {
        Logger.v(activity.logTag, "onResumed()")
    }

    override fun onActivityStarted(activity: Activity) {
        Logger.v(activity.logTag, "onStarted()")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Logger.v(activity.logTag, "onDestroyed()")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Logger.v(activity.logTag, "onSaveInstanceState()")
    }

    override fun onActivityStopped(activity: Activity) {
        Logger.v(activity.logTag, "onStopped()")
    }
})