package net.ketc.numeri.domain.model.cache

import net.ketc.numeri.domain.service.TwitterClient
import net.ketc.numeri.util.log.v
import twitter4j.Friendship
import twitter4j.Relationship

object UserRelationCache {
    private val map = HashMap<Long, HashMap<Long, UserRelation>>()
    private fun getOrPut(clientId: Long) = map.getOrPut(clientId) { HashMap() }

    private fun getRelation(clientId: Long, targetUserId: Long): UserRelation? {
        val relationMap = getOrPut(clientId)
        return relationMap[targetUserId]
    }

    fun create(relationship: Relationship): UserRelation {
        val relation = getRelation(relationship.sourceUserId, relationship.targetUserId)?.apply {
            update(relationship)
            v("relation cache", "update")
        }

        fun put(): UserRelation {
            val value = UserRelationImpl(relationship)
            getOrPut(relationship.sourceUserId).put(value.targetUserId, value)
            v("relation cache", "new")
            return value
        }
        return relation ?: put()
    }

    fun create(client: TwitterClient, friendship: Friendship): UserRelation {
        val relation = getRelation(client.id, friendship.id)?.apply { update(client, friendship) }
        fun put(): UserRelation {
            val value = UserRelationImpl(client, friendship)
            getOrPut(client.id).put(value.targetUserId, value)
            return value
        }
        return relation ?: put()
    }
}

fun Relationship.convert() = UserRelationCache.create(this)

fun Friendship.convert(client: TwitterClient) = UserRelationCache.create(client, this)


private class UserRelationImpl : UserRelation {
    override val sourceUserId: Long
    override val targetUserId: Long
    override var type: RelationType
        private set

    constructor(relationship: Relationship) {
        sourceUserId = relationship.sourceUserId
        targetUserId = relationship.targetUserId
        type = relationship.toRelationType()
    }

    constructor(client: TwitterClient, friendship: Friendship) {
        sourceUserId = client.id
        targetUserId = friendship.id
        type = friendship.toRelationType()
    }

    override fun update(relationship: Relationship) {
        if (relationship.targetUserId == targetUserId
                && relationship.sourceUserId == sourceUserId)
            type = relationship.toRelationType()
        else throw IllegalArgumentException()
    }

    override fun update(client: TwitterClient, friendship: Friendship) {
        if (friendship.id == targetUserId
                && client.id == sourceUserId)
            type = friendship.toRelationType()
        else throw IllegalArgumentException()
    }
}

private fun Relationship.toRelationType(): RelationType {
    return when {
        isSourceBlockingTarget -> RelationType.BLOCkING
        isSourceFollowedByTarget && isSourceFollowingTarget -> RelationType.MUTUAL
        isSourceFollowedByTarget -> RelationType.FOLLOWED
        isSourceFollowingTarget -> RelationType.FOLLOWING
        else -> RelationType.NOTHING
    }
}

private fun Friendship.toRelationType(): RelationType {
    return when {
        isFollowing && isFollowedBy -> RelationType.MUTUAL
        isFollowedBy -> RelationType.FOLLOWED
        isFollowing -> RelationType.FOLLOWING
        else -> RelationType.NOTHING
    }
}