package net.ketc.numeri.domain.model

import net.ketc.numeri.domain.model.cache.Cacheable

interface TwitterUser : Cacheable<Long> {
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
    val urlEntities: List<UrlEntity>
}