package tech.ketc.numeri.domain.twitter.model

import java.net.URI

data class UserList(override val id: Long, override var name: String, override var fullName: String,
                    override var slug: String, override var description: String, override val uri: URI) : IUserList {
    constructor(userList: twitter4j.UserList) : this(userList.id,
            userList.name ?: "",
            userList.fullName ?: "",
            userList.slug ?: "",
            userList.description ?: "",
            userList.uri)
}