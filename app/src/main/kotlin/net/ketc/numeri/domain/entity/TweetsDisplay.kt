package net.ketc.numeri.domain.entity

import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import net.ketc.numeri.util.ormlite.Entity

@DatabaseTable
data class TweetsDisplayGroup(@DatabaseField(generatedId = true)
                              override val id: Int = 0) : Entity<Int>

data class TweetsDisplay(
        @DatabaseField(generatedId = true)
        override val id: Int = 0,
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        val token: ClientToken = ClientToken(),
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        val group: TweetsDisplayGroup = TweetsDisplayGroup(),
        @DatabaseField(canBeNull = false, uniqueCombo = true)
        val foreignId: Long = -1,
        @DatabaseField(canBeNull = false, uniqueCombo = true)
        val type: TweetsDisplayType = TweetsDisplayType.HOME,
        @DatabaseField(canBeNull = false)
        var order: Int = 0) : Entity<Int>

fun createTweetsDisplay(token: ClientToken, group: TweetsDisplayGroup, foreignId: Long, type: TweetsDisplayType)
        = TweetsDisplay(token = token, group = group, foreignId = foreignId, type = type)

enum class TweetsDisplayType {
    HOME, MENTIONS, USER_LIST, PUBLIC
}