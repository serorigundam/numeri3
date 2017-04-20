package net.ketc.numeri.presentation.presenter.fragment

import net.ketc.numeri.domain.inject
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.presentation.view.component.TweetsDisplayPagerAdapter
import net.ketc.numeri.presentation.view.fragment.TimeLinesFragmentInterface
import javax.inject.Inject

class TimeLinesPresenter(override val fragment: TimeLinesFragmentInterface) : AutoDisposableFragmentPresenter<TimeLinesFragmentInterface>() {
    @Inject
    lateinit var oAuthService: OAuthService
    @Inject
    lateinit var displayService: TweetsDisplayService

    init {
        inject()
    }

    override fun initialize() {
        super.initialize()
        val adapter = TweetsDisplayPagerAdapter(fragment.fm,
                fragment.group, this, displayService)
        fragment.setAdapter(adapter)
    }
}