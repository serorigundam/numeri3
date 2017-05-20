package net.ketc.numeri.presentation.presenter.fragment.dialog

import net.ketc.numeri.presentation.presenter.fragment.AutoDisposableFragmentPresenter
import net.ketc.numeri.presentation.view.fragment.dialog.TweetOperateDialogFragmentInterface
import org.jetbrains.anko.toast

class TweetOperateDialogPresenter(override val fragment: TweetOperateDialogFragmentInterface)
    : AutoDisposableFragmentPresenter<TweetOperateDialogFragmentInterface>() {
    fun onError(throwable: Throwable) {
        fragment.activity.toast(throwable.message ?: "error")
    }
}