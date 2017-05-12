package net.ketc.numeri.domain.model

import java.io.Serializable

data class UserMentionEntity(val id: Long, val screenName: String, val start: Int, val end: Int) : Serializable {
    constructor(entity: twitter4j.UserMentionEntity) : this(entity.id, entity.screenName, entity.start, entity.end)
}