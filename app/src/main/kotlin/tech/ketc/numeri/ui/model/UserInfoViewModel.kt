package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.IUserHandler
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.model.delegate.UserHandler
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import twitter4j.Relationship
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class UserInfoViewModel @Inject constructor(twitterUserRepository: ITwitterUserRepository,
                                            imageRepository: IImageRepository)
    : ViewModel(),
        IUserHandler by UserHandler(twitterUserRepository),
        IImageLoadable by ImageLoadable(imageRepository) {

    private val mRelation: Relation? = null
    private val mLock = ReentrantLock()

    fun loadRelation(client: TwitterClient, targetId: Long) = asyncResponse {
        mLock.withLock {
            mRelation ?: client.twitter.showFriendship(client.id, targetId).let(::Relation)
        }
    }


    class Relation(relationship: Relationship) {
        val isFollowing: Boolean = relationship.isSourceFollowingTarget
        val isFollowed: Boolean = relationship.isSourceFollowedByTarget
        val isBlocking: Boolean = relationship.isSourceBlockingTarget
        val isMuting: Boolean = relationship.isSourceMutingTarget
        val isMutual: Boolean = isFollowing && isFollowed
    }
}