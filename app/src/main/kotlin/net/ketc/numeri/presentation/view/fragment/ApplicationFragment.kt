package net.ketc.numeri.presentation.view.fragment

import android.app.Activity
import android.support.v4.app.Fragment
import net.ketc.numeri.presentation.presenter.fragment.FragmentPresenter

abstract class ApplicationFragment<P : FragmentPresenter<FragmentInterface>> : Fragment() {
    internal abstract var presenter: P

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.onDestroyView()
    }
}

interface FragmentInterface {
    val activity: Activity
}