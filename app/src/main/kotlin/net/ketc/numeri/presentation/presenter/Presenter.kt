package net.ketc.numeri.presentation.presenter

import android.content.Context
import android.os.Bundle
import io.reactivex.disposables.Disposable
import net.ketc.numeri.presentation.view.ActivityInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl
import net.ketc.numeri.presentation.view.ApplicationActivity

/**
 * Presenter
 */
interface Presenter<out T : ActivityInterface> {
    val activity: T
    val ctx: Context
        get() = activity.ctx

    /**
     * this method is called at an arbitrary timing and initializes own instance
     */
    fun initialize() {
    }

    /**
     * This method is bound to [ApplicationActivity.onPause]
     */
    fun onPause() {
    }

    /**
     * This method is bound to [ApplicationActivity.onSaveInstanceState]
     */
    fun onSaveInstanceState(bundle: Bundle) {
    }

    /**
     * This method is bound to [ApplicationActivity.onRestoreInstanceState]
     */
    fun onRestoreInstanceState(bundle: Bundle) {
    }

    /**
     * This method is bound to [ApplicationActivity.onResume]
     */
    fun onResume() {
    }

    /**
     * This method is bound to [ApplicationActivity.onDestroy]
     */
    fun onDestroy() {
    }
}

/**
 * This class executes all of its [Disposable.dispose] of [Disposable] with [ApplicationActivity.onDestroy].
 **/
abstract class AutoDisposablePresenter<out T : ActivityInterface> : Presenter<T>, AutoDisposable by AutoDisposableImpl() {
    override fun onDestroy() = dispose()
}