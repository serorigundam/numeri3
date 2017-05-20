package net.ketc.numeri.presentation.presenter.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import io.reactivex.disposables.Disposable
import net.ketc.numeri.activityManger
import net.ketc.numeri.presentation.view.activity.ActivityInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl
import net.ketc.numeri.presentation.view.activity.ApplicationActivity
import net.ketc.numeri.util.log.v
import net.ketc.numeri.util.rx.SingleTask
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * Presenter
 */
interface Presenter<T : ActivityInterface> {

    var activity: T

    val ctx: Context
        get() = activity.ctx

    /**
     * this method is called at an arbitrary timing and initializes own instance
     */
    fun initialize(savedInstanceState: Bundle? = null, isStartedForFirst: Boolean = false) {

    }

    /**
     * This method is bound to [ApplicationActivity.onPause]
     */
    fun onPause() {
        v(this.javaClass.simpleName, "onPause")
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
        v(this.javaClass.simpleName, "onResume")
    }

    /**
     * This method is bound to [ApplicationActivity.onDestroy]
     */
    fun onDestroy(isFinishing: Boolean) {
        v(this.javaClass.simpleName, "onDestroy")
    }
}

abstract class AbstractPresenter<AI : ActivityInterface> : Presenter<AI> {
    override var activity: AI
        get() = mActivity ?: throw IllegalStateException()
        set(value) {
            mActivity = value
        }

    private var mActivity: AI? = null

    override fun onDestroy(isFinishing: Boolean) {
        mActivity = null
        super.onDestroy(isFinishing)
    }
}

abstract class PresenterFactory<out P : Presenter<*>> {

    private val map = LinkedHashMap<UUID, P>()

    fun createOrGet(activityId: UUID): P {
        val p = map.getOrPut(activityId) { create() }
        return p
    }

    protected abstract fun create(): P

    fun finishActivity(activityId: UUID) {
        map.remove(activityId)
    }
}

/**
 * This class executes all of its [Disposable.dispose] of [Disposable] with [ApplicationActivity.onDestroy].
 **/
abstract class AutoDisposablePresenter<T : ActivityInterface> : AbstractPresenter<T>(), AutoDisposable by AutoDisposableImpl() {

    fun safePost(task: (T) -> Unit) {
        val activity = ctx as Activity
        val activityManger = activity.application.activityManger
        val activityId = activityManger.getActivityId(activity)
        activityManger.getSafePostDelegate(activityId).safePost {
            task(this@AutoDisposablePresenter.activity)
        }
    }

    infix fun <R> SingleTask<R>.safeSuccess(success: T.(R) -> Unit) = this.success { result ->
        safePost { activity ->
            activity.success(result)
        }
    }

    infix fun <R> SingleTask<R>.safeError(success: T.(Throwable) -> Unit) = this.error { throwable ->
        safePost { activity ->
            activity.success(throwable)
        }
    }

    override fun onDestroy(isFinishing: Boolean) {
        if (isFinishing)
            dispose()
        super.onDestroy(isFinishing)
    }
}