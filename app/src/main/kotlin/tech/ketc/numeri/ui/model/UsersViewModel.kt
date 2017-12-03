package tech.ketc.numeri.ui.model

import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.repository.ITwitterUserRepository
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.ClientUserRelation
import tech.ketc.numeri.domain.twitter.model.UserRelation
import tech.ketc.numeri.ui.model.delegate.IFriendshipHandler
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.coroutine.asyncResponse
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableList
import twitter4j.PagableResponseList
import twitter4j.User
import java.io.Serializable
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class UsersViewModel @Inject constructor(private val mUsersRepository: ITwitterUserRepository,
                                         private val imageRepository: IImageRepository)
    : ViewModel(),
        IFriendshipHandler,
        IImageLoadable by ImageLoadable(imageRepository) {
    companion object {
        private val MAX = 100
    }

    private var mType: Type? = null

    fun setType(type: Type) {
        mType = type
    }

    private val mMap = HashMap<Long, Pair<List<ClientUserRelation>, Long>>()
    private val mStoredRelations = ArrayList<ClientUserRelation>()
    val storedRelations: List<ClientUserRelation>
        get() = mStoredRelations.unmodifiableList()
    private val mLock = ReentrantLock()

    private fun PagableResponseList<User>.toUserRelationList(client: TwitterClient): Pair<List<ClientUserRelation>, Long> {
        Logger.v(logTag, "toUserRelationList")
        val users = this.map { mUsersRepository.createOrGet(it) }
        val userIDs = this.map { it.id }
        val friendShips = client.twitter.lookupFriendships(*userIDs.toLongArray())
        val filtered = users.filter { user -> friendShips.any { user.id == it.id } }
        return filtered.mapIndexed { i, user ->
            ClientUserRelation(user, UserRelation(friendShips[i])).also {
                mStoredRelations.add(it)
            }
        } to this.nextCursor
    }


    fun getClientUserRelationList(client: TwitterClient, targetId: Long, cursor: Long = -1) = asyncResponse {
        mLock.withLock {
            Logger.v(logTag, "getClientUserRelationList $cursor")
            val type = mType ?: throw IllegalStateException()
            mMap[cursor] ?: when (type) {
                Type.FOLLOWERS -> client.twitter.getFollowersList(targetId, cursor, MAX)
                Type.FOLLOWS -> client.twitter.getFriendsList(targetId, cursor, MAX)
            }.toUserRelationList(client).also { mMap.put(cursor, it) }
        }
    }

    enum class Type : Serializable {
        FOLLOWS, FOLLOWERS
    }
}