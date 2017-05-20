package net.ketc.numeri.presentation.presenter.activity

import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.activity.CreateDisplayGroupActivityInterface
import javax.inject.Inject

object CreateDisplayGroupPresenterFactory : PresenterFactory<CreateDisplayGroupPresenter>() {
    override fun create() = CreateDisplayGroupPresenter()
}

class CreateDisplayGroupPresenter : AbstractPresenter<CreateDisplayGroupActivityInterface>() {
    @Inject
    lateinit var displayService: TweetsDisplayService

    init {
        inject()
    }

    fun addGroup(name: String) {
        displayService.createGroup(name)
    }
}