package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.model.TwitterUser
import net.ketc.numeri.domain.model.UserList
import net.ketc.numeri.domain.service.TwitterClient

object UserListCache {
    private val map = LinkedHashMap<Long, MutableList<UserList>>()
    private fun getOrPut(user: TwitterUser): MutableList<UserList> {
        var list = map[user.id]
        if (list == null) {
            list = ArrayList<UserList>()
            map.put(user.id, list)
        }
        return list
    }

    fun put(user: TwitterUser, userList: UserList) {
        getOrPut(user).add(userList)
    }

    fun get(user: TwitterUser): List<UserList>? = map[user.id]

}

fun TwitterClient.userLists(): List<UserList> {
    val user = this.user()
    var list = UserListCache.get(user)
    if (list == null) {
        list = this.twitter.getUserLists(this.id).map(::UserList)
        list.forEach { UserListCache.put(user, it) }
    }
    return list
}