package tech.ketc.numeri.domain.twitter.model

import tech.ketc.numeri.domain.twitter.ITwitterUserFactory
import tech.ketc.numeri.domain.twitter.client.TwitterClient
import java.net.URI

data class UserList(override val id: Long, override val user: TwitterUser, override var name: String, override var fullName: String,
                    override var slug: String, override var description: String, override val uri: URI) : IUserList {
    constructor(client: TwitterClient, userList: twitter4j.UserList, factory: ITwitterUserFactory) : this(userList.id,
            factory.createOrGet(client, userList.user),
            userList.name ?: "",
            userList.fullName ?: "",
            userList.slug ?: "",
            userList.description ?: "",
            userList.uri)
}