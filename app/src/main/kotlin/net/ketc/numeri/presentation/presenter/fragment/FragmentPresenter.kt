package net.ketc.numeri.presentation.presenter.fragment

import android.app.Activity
import net.ketc.numeri.presentation.view.fragment.FragmentInterface
import net.ketc.numeri.util.rx.AutoDisposable
import net.ketc.numeri.util.rx.AutoDisposableImpl

interface FragmentPresenter<out FI : FragmentInterface> {

    val fragment: FI

    val parent: Activity
        get() = fragment.activity

    fun initialize() {
    }

    fun onPause() {
    }

    fun onResume() {
    }

    fun onDestroyView() {
    }
}

abstract class AutoDisposableFragmentPresenter<out FI : FragmentInterface>(autoDisposable: AutoDisposable = AutoDisposableImpl()) : FragmentPresenter<FI>, AutoDisposable by autoDisposable {
    override fun onDestroyView() = dispose()
}