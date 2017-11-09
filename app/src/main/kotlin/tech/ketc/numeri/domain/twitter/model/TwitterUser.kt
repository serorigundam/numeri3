package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable

interface TwitterUser : Serializable {
    val id: Long
    val name: String
    val screenName: String
    val location: String
    val description: String
    val iconUrl: String
    val originalIconUrl: String
    val headerImageUrl: String?
    val profileBackgroundColor: String
    val isProtected: Boolean
    val followersCount: Int
    val friendsCount: Int
    val statusesCount: Int
    val favoriteCount: Int
    val urlEntities: List<IUrlEntity>
}