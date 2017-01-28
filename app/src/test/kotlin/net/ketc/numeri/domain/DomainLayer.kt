package net.ketc.numeri.domain

import dagger.Component
import dagger.Module
import dagger.Provides
import net.ketc.numeri.domain.entity.TweetsDisplay
import net.ketc.numeri.domain.entity.TweetsDisplayGroup
import net.ketc.numeri.domain.service.*
import net.ketc.numeri.setOnMemoryDB
import net.ketc.numeri.util.ormlite.createTable
import javax.inject.Singleton

@Module
class TestDomainModule {

    @Provides
    @Singleton
    fun provideOAuthService(): OAuthService = OAuthServiceImpl()

    @Provides
    @Singleton
    fun provideTweetDisplayService(): TweetsDisplayService {
        setOnMemoryDB()
        createTable(TweetsDisplay::class,  TweetsDisplayGroup::class)
        return TweetsDisplayServiceImpl()
    }

}

@Singleton
@Component(modules = arrayOf(TestDomainModule::class))
interface TestDomainComponent {
    fun inject(oAuthServiceSpecDependency: OAuthServiceTest)
    fun inject(tweetsDisplayServiceTest: TweetsDisplayServiceTest)
}