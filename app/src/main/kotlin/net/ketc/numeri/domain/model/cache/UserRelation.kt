package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.service.TwitterClient
import twitter4j.Friendship
import twitter4j.Relationship

interface UserRelation {
    val sourceUserId: Long
    val targetUserId: Long
    val type: RelationType
    fun update(relationship: Relationship)
    fun update(client: TwitterClient, friendship: Friendship)
}

enum class RelationType {
    MUTUAL, FOLLOWING, FOLLOWED, BLOCkING, NOTHING
}