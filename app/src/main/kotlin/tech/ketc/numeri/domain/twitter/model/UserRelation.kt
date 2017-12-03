package tech.ketc.numeri.domain.twitter.model

import twitter4j.Friendship

class UserRelation(friendship: Friendship) {
    val id = friendship.id
    var isFollowing = friendship.isFollowing
}