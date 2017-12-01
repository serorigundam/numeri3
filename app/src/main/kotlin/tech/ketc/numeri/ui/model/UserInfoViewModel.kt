package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.IUserHandler
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.ui.model.delegate.UserHandler
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.logTag
import twitter4j.Relationship
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class UserInfoViewModel @Inject constructor(twitterUserRepository: ITwitterUserRepository,
                                            private val mImageRepository: IImageRepository)
    : ViewModel(),
        IUserHandler by UserHandler(twitterUserRepository),
        IImageLoadable by ImageLoadable(mImageRepository) {

    private val mRelation: Relation? = null
    private val mLoadRelationLock = ReentrantLock()
    private val mLoadHeaderLock = ReentrantLock()
    private var mHeaderImage: BitmapContent? = null

    fun loadRelation(client: TwitterClient, targetId: Long) = asyncResponse {
        mLoadRelationLock.withLock {
            mRelation ?: client.twitter.showFriendship(client.id, targetId).let(::Relation) as IRelation
        }
    }

    fun updateRelation(relation: IRelation, client: TwitterClient, targetId: Long) = asyncResponse {
        val following = relation.isFollowing
        if (following)
            client.twitter.destroyFriendship(targetId)
        else
            client.twitter.createFriendship(targetId)
        Logger.v(logTag, "result following ${!following}")
        relation.apply { (this as Relation).mIsFollowing = !following }
    }

    fun loadHeaderImage(user: TwitterUser): ResponseDeferred<BitmapContent>? {
        fun blockingLoad(url: String) = asyncResponse {
            mLoadHeaderLock.withLock {
                mHeaderImage ?: mImageRepository.downloadOrGet(url, false).also { mHeaderImage = it }
            }
        }
        return user.headerImageUrl?.let(::blockingLoad)
    }

    interface IRelation {
        val isFollowing: Boolean
        val isFollowed: Boolean
        val isBlocking: Boolean
        val isMuting: Boolean
        val isMutual: Boolean
    }

    private class Relation(relationship: Relationship) : IRelation {
        var mIsFollowing: Boolean = relationship.isSourceFollowingTarget
        override val isFollowing: Boolean
            get() = mIsFollowing
        override val isFollowed: Boolean = relationship.isSourceFollowedByTarget
        override val isBlocking: Boolean = relationship.isSourceBlockingTarget
        override val isMuting: Boolean = relationship.isSourceMutingTarget
        override val isMutual: Boolean = isFollowing && isFollowed
    }
}