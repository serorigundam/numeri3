package net.ketc.numeri.presentation.presenter.fragment

import android.app.Activity
import net.ketc.numeri.activityManger
import net.ketc.numeri.presentation.view.fragment.FragmentInterface
import net.ketc.numeri.util.log.v
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl
import net.ketc.numeri.util.rx.SingleTask

interface FragmentPresenter<out FI : FragmentInterface> {

    val fragment: FI

    val parent: Activity
        get() = fragment.activity

    fun initialize() {
    }

    fun onPause() {
        v(this.javaClass.simpleName, "onPause")
    }

    fun onResume() {
        v(this.javaClass.simpleName, "onResume")
    }

    fun onDestroyView() {
        v(this.javaClass.simpleName, "onDestroyView")
    }
}

abstract class AutoDisposableFragmentPresenter<out FI : FragmentInterface>(autoDisposable: AutoDisposable = AutoDisposableImpl()) : FragmentPresenter<FI>, AutoDisposable by autoDisposable {
    fun safePost(task: (FI) -> Unit) {
        val activity = parent
        val activityManger = activity.application.activityManger
        val activityId = activityManger.getActivityId(activity)
        activityManger.getSafePostDelegate(activityId).safePost {
            task(this@AutoDisposableFragmentPresenter.fragment)
        }
    }

    infix fun <R> SingleTask<R>.safeSuccess(success: FI.(R) -> Unit) = this.success { result ->
        safePost { fragment ->
            fragment.success(result)
        }
    }

    infix fun <R> SingleTask<R>.safeError(success: FI.(Throwable) -> Unit) = this.error { throwable ->
        safePost { fragment ->
            fragment.success(throwable)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (parent.isFinishing) {
            dispose()
        }
    }
}