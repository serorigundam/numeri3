package net.ketc.numeri.presentation.presenter.activity

import net.ketc.numeri.presentation.view.activity.MediaActivityInterface

object MediaPresenterFactory : PresenterFactory<MediaPresenter>() {
    override fun create() = MediaPresenter()
}

class MediaPresenter
    : AutoDisposablePresenter<MediaActivityInterface>()