package net.ketc.numeri.presentation.presenter.fragment

import android.app.Activity
import net.ketc.numeri.presentation.view.fragment.FragmentInterface
import net.ketc.numeri.util.android.SafePostDelegate
import net.ketc.numeri.util.log.v
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl

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
    private val safePostDelegate = SafePostDelegate()


    fun safePost(task: () -> Unit) = safePostDelegate.safePost(task)


    override fun onPause() {
        super.onPause()
        safePostDelegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        safePostDelegate.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dispose()
    }
}