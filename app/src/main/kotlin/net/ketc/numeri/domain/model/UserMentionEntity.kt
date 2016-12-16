package net.ketc.numeri.domain.model

data class UserMentionEntity(val id: Long, val screenName: String, val start: Int, val end: Int) {
    constructor(entity: twitter4j.UserMentionEntity) : this(entity.id, entity.screenName, entity.start, entity.end)
}