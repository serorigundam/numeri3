package tech.ketc.numeri.domain.twitter.model

data class ClientUserRelation(val user: TwitterUser, val relation: UserRelation) {
    init {
        if (user.id != relation.id) throw IllegalArgumentException()
    }
}