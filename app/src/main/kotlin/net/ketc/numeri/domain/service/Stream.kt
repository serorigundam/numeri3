package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.model.Tweet
import net.ketc.numeri.domain.model.TwitterUser
import twitter4j.DirectMessage
import twitter4j.StallWarning
import twitter4j.StatusDeletionNotice
import twitter4j.UserList

data class StatusNotice(val source: TwitterUser, val target: TwitterUser, val tweet: Tweet)

data class UserNotice(val source: TwitterUser, val target: TwitterUser)

data class UserListMemberNotice(val source: TwitterUser, val target: TwitterUser, val list: UserList)

data class UserListNotice(val source: TwitterUser, val list: UserList)

data class IdNotice(val id1: Long, val id2: Long)

interface StreamAdapter {

    fun onUserListMemberAddition(notice: UserListMemberNotice)

    fun onFavorite(notice: StatusNotice)

    fun onBlock(notice: UserNotice)

    fun onUserListUpdate(notice: UserListNotice)

    fun onUserListSubscription(notice: UserListMemberNotice)

    fun onQuotedTweet(notice: StatusNotice)

    fun onDeletionNotice(notice: IdNotice)

    fun onUserProfileUpdate(user: TwitterUser)

    fun onDirectMessage(dm: DirectMessage)

    fun onUserListUnsubscription(notice: UserListMemberNotice)

    fun onFollow(notice: UserNotice)

    fun onUserListMemberDeletion(notice: UserListMemberNotice)

    fun onUserListDeletion(notice: UserListNotice)

    fun onUnfollow(notice: UserNotice)

    fun onRetweetedRetweet(notice: StatusNotice)

    fun onUserListCreation(notice: UserListNotice)

    fun onFavoritedRetweet(notice: StatusNotice)

    fun onUnfavorite(notice: StatusNotice)

    fun onUserDeletion(id: Long)

    fun onFriendList(ids: LongArray)

    fun onUnblock(notice: UserNotice)

    fun onUserSuspension(id: Long)

    fun onScrubGeo(notice: IdNotice)

    fun onDeletionNotice(notice: StatusDeletionNotice)

    fun onTrackLimitationNotice(numberOfLimitedStatuses: Int)

    fun onStallWarning(stallWarning: StallWarning)

    fun onStatus(tweet: Tweet)

    fun onException(e: Exception)
}

abstract class AbstractStreamAdapter() : StreamAdapter {
    override fun onUserListMemberAddition(notice: UserListMemberNotice) {
    }

    override fun onFavorite(notice: StatusNotice) {
    }

    override fun onBlock(notice: UserNotice) {
    }

    override fun onUserListUpdate(notice: UserListNotice) {
    }

    override fun onUserListSubscription(notice: UserListMemberNotice) {
    }

    override fun onQuotedTweet(notice: StatusNotice) {
    }

    override fun onDeletionNotice(notice: IdNotice) {
    }

    override fun onUserProfileUpdate(user: TwitterUser) {
    }

    override fun onDirectMessage(dm: DirectMessage) {
    }

    override fun onUserListUnsubscription(notice: UserListMemberNotice) {
    }

    override fun onFollow(notice: UserNotice) {
    }

    override fun onUserListMemberDeletion(notice: UserListMemberNotice) {
    }

    override fun onUserListDeletion(notice: UserListNotice) {
    }

    override fun onUnfollow(notice: UserNotice) {
    }

    override fun onRetweetedRetweet(notice: StatusNotice) {
    }

    override fun onUserListCreation(notice: UserListNotice) {
    }

    override fun onFavoritedRetweet(notice: StatusNotice) {
    }

    override fun onUnfavorite(notice: StatusNotice) {
    }

    override fun onUserDeletion(id: Long) {
    }

    override fun onFriendList(ids: LongArray) {
    }

    override fun onUnblock(notice: UserNotice) {
    }

    override fun onUserSuspension(id: Long) {
    }

    override fun onScrubGeo(notice: IdNotice) {
    }

    override fun onDeletionNotice(notice: StatusDeletionNotice) {
    }

    override fun onTrackLimitationNotice(numberOfLimitedStatuses: Int) {
    }

    override fun onStallWarning(stallWarning: StallWarning) {
    }

    override fun onStatus(tweet: Tweet) {
    }

    override fun onException(e: Exception) {
    }

}