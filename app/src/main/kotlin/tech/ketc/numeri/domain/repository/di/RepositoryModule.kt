package tech.ketc.numeri.domain.repository.di

import dagger.Module
import dagger.Provides
import tech.ketc.numeri.App
import tech.ketc.numeri.domain.repository.*
import tech.ketc.numeri.domain.twitter.*
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.infra.ImageDatabase
import tech.ketc.numeri.ui.model.di.ViewModelComponent
import javax.inject.Singleton

@Module(subcomponents = [ViewModelComponent::class])
class RepositoryModule {

    @Singleton
    @Provides
    fun provideAccountRepository(app: App,
                                 db: AccountDatabase,
                                 oauthSupportFactory: IOAuthSupportFactory,
                                 twitterClientFactory: ITwitterClientFactory,
                                 timelineInfoRepository: ITimelineInfoRepository): IAccountRepository
            = AccountRepository(app, db, oauthSupportFactory, twitterClientFactory, timelineInfoRepository)

    @Singleton
    @Provides
    fun provideTweetRepository(tweetFactory: ITweetFactory,
                               stateFactory: ITweetStateFactory,
                               userFactory: ITwitterUserFactory): ITweetRepository
            = TweetRepository(tweetFactory, stateFactory, userFactory)

    @Singleton
    @Provides
    fun provideTwitterUserRepository(factory: ITwitterUserFactory,
                                     tweetRepository: ITweetRepository): ITwitterUserRepository
            = TwitterUserRepository(factory, tweetRepository)

    @Singleton
    @Provides
    fun provideImageRepository(db: ImageDatabase): IImageRepository
            = ImageRepository(db)

    @Singleton
    @Provides
    fun provideTwitterStreamRepository(factory: ITwitterStreamFactory,
                                       userRepository: ITwitterUserRepository,
                                       tweetRepository: ITweetRepository): ITwitterStreamRepository
            = TwitterStreamRepository(factory, userRepository, tweetRepository)

    @Singleton
    @Provides
    fun provideTimelineRepository(db: AccountDatabase): ITimelineInfoRepository = TimelineInfoRepository(db)
}