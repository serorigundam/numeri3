package net.ketc.numeri.domain.model

import java.net.URI

data class UserList(val id: Long, var name: String, var fullName: String, var slug: String, var description: String, val uri: URI) {
    constructor(userList: twitter4j.UserList) : this(userList.id,
            userList.name ?: "",
            userList.fullName ?: "",
            userList.slug ?: "",
            userList.description ?: "",
            userList.uri)
}