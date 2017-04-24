package net.ketc.numeri.domain

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.Injectors
import net.ketc.numeri.domain.service.OAuthService
import net.ketc.numeri.domain.service.OAuthServiceImpl
import net.ketc.numeri.domain.service.TweetsDisplayService
import net.ketc.numeri.domain.service.TweetsDisplayServiceImpl
import net.ketc.numeri.presentation.presenter.activity.*
import net.ketc.numeri.presentation.presenter.fragment.TimeLinesPresenter
import net.ketc.numeri.presentation.presenter.fragment.tweet.display.TimeLinePresenter
import javax.inject.Singleton

@Module
class DomainModule {

    @Provides
    @Singleton
    fun provideOAuthService(): OAuthService = OAuthServiceImpl()


    @Provides
    @Singleton
    fun provideTweetDisplayService(): TweetsDisplayService = TweetsDisplayServiceImpl()
}

@Singleton
@Component(modules = arrayOf(DomainModule::class))
interface DomainComponent {
    fun inject(mainPresenter: MainPresenter)
    fun inject(timeLinePresenter: TimeLinePresenter)
    fun inject(timeLinesPresenter: TimeLinesPresenter)
    fun inject(tweetsDisplayGroupManagePresenter: TweetsDisplayGroupManagePresenter)
    fun inject(tweetsDisplayManagePresenter: TweetsDisplayManagePresenter)
    fun inject(createDisplayGroupPresenter: CreateDisplayGroupPresenter)
    fun inject(conversationPresenter: ConversationPresenter)
}

//extension
fun MainPresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun TimeLinePresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun TimeLinesPresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun TweetsDisplayGroupManagePresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun TweetsDisplayManagePresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun CreateDisplayGroupPresenter.inject() {
    Injectors.domainComponent.inject(this)
}

fun ConversationPresenter.inject() {
    Injectors.domainComponent.inject(this)
}
