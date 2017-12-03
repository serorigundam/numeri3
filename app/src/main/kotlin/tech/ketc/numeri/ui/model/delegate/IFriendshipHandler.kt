package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.twitter.client.TwitterClient
import tech.ketc.numeri.domain.twitter.model.ClientUserRelation
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

interface IFriendshipHandler {
    fun updateFriendship(client: TwitterClient, userRelation: ClientUserRelation) = asyncResponse {
        val id = userRelation.user.id
        val relation = userRelation.relation
        val isFollowing = relation.isFollowing
        if (isFollowing)
            client.twitter.destroyFriendship(id)
        else
            client.twitter.createFriendship(id)
        relation.isFollowing = !isFollowing
    }
}