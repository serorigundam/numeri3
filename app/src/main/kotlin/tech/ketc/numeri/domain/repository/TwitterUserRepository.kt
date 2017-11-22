package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.domain.twitter.UserDeleteListener
import tech.ketc.numeri.domain.twitter.UserUpdateListener
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.UserList
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.livedata.mediate
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableList
import twitter4j.User
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

class TwitterUserRepository @Inject constructor(private val mUserFactory: ITwitterUserFactory,
                                                private val mTweetRepository: ITweetRepository) : ITwitterUserRepository {

    private val mUpdateListener: UserUpdateListener = {
        mLatestUpdatedUser.postValue(it)
    }

    private val mDeleteListener: UserDeleteListener = {
        mLatestDeletedUser.postValue(it)
    }

    private val mUserListsMap = LinkedHashMap<TwitterUser, List<UserList>>()

    init {
        mUserFactory.addUpdateListener(mUpdateListener)
        mUserFactory.addDeleteListener(mDeleteListener)
    }

    private val mLatestUpdatedUser = MutableLiveData<TwitterUser>()
    private val mLatestDeletedUser = MutableLiveData<TwitterUser>()

    override val latestUpdatedUser: LiveData<TwitterUser>
        get() = mLatestUpdatedUser.mediate()

    override val latestDeletedUser: LiveData<TwitterUser>
        get() = mLatestDeletedUser.mediate()

    private val mLockMap = LinkedHashMap<TwitterUser, ReentrantLock>()

    override fun createOrGet(user: User): TwitterUser {
        return mUserFactory.createOrGet(user)
    }

    override fun show(client: TwitterClient, id: Long): TwitterUser {
        return mUserFactory.get(id) ?: createOrGet(client.twitter.showUser(id))
    }

    override fun delete(user: TwitterUser) {
        mTweetRepository.deleteByUser(user)
        mUserFactory.delete(user)
    }

    override fun getUserList(client: TwitterClient, user: TwitterUser): List<UserList> {
        val lock = mLockMap.getOrPut(user) { ReentrantLock() }
        return lock.withLock {
            var list = mUserListsMap[user]
            if (list == null) {
                list = client.twitter.getUserLists(user.id).map { UserList(it, mUserFactory) }.unmodifiableList()
                mUserListsMap.put(user, list)
                Logger.v(logTag, "getUserList() request")
            }
            return@withLock list
        }
    }

    override fun reloadUserList(client: TwitterClient, user: TwitterUser): List<UserList> {
        val lock = mLockMap.getOrPut(user) { ReentrantLock() }
        return lock.withLock {
            val list = client.twitter.getUserLists(user.id).map { UserList(it, mUserFactory) }.unmodifiableList()
            mUserListsMap.put(user, list)
            return@withLock list
        }
    }
}