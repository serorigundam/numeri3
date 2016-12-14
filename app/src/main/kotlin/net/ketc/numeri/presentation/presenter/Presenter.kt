package net.ketc.numeri.presentation.presenter

import android.content.Context
import net.ketc.numeri.presentation.view.ActivityInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl

/**
 * Presenter
 */
interface Presenter<out T : ActivityInterface> {
    val activity: T
    val ctx: Context
        get() = activity.ctx

    fun initialize() {
    }

    fun onPause() {
    }

    fun onResume() {
    }

    fun onDestroy() {
    }
}

abstract class AutoDisposablePresenter<out T : ActivityInterface> : Presenter<T>, AutoDisposable by AutoDisposableImpl() {
    override fun onDestroy() = dispose()
}