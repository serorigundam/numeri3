package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable
import java.net.URI

interface IUserList : Serializable {
    val id: Long
    val user: TwitterUser
    var name: String
    var fullName: String
    var slug: String
    var description: String
    val uri: URI
}