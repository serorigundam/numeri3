package net.ketc.numeri.presentation.presenter.fragment

import net.ketc.numeri.presentation.view.fragment.TimeLineFragmentInterface

class TimeLinePresenter(timeLineFragment: TimeLineFragmentInterface) : AutoDisposableFragmentPresenter<TimeLineFragmentInterface>() {
    override val fragment: TimeLineFragmentInterface = timeLineFragment

    override fun initialize() {
        super.initialize()
    }
}