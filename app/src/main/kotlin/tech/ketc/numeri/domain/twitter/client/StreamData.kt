package tech.ketc.numeri.domain.twitter.client

import tech.ketc.numeri.domain.twitter.model.TwitterUser
import tech.ketc.numeri.domain.twitter.model.Tweet
import tech.ketc.numeri.domain.twitter.model.UserList

data class StatusNotice(val source: TwitterUser, val target: TwitterUser, val tweet: Tweet)

data class UserNotice(val source: TwitterUser, val target: TwitterUser)

data class UserListMemberNotice(val source: TwitterUser, val target: TwitterUser, val list: UserList)

data class UserListNotice(val source: TwitterUser, val list: UserList)

data class IdNotice(val id1: Long, val id2: Long)