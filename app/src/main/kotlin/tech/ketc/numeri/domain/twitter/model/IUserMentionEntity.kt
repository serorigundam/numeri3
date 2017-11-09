package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable

interface IUserMentionEntity : Serializable {
    val id: Long
    val screenName: String
    val start: Int
    val end: Int
}