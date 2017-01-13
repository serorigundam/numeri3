package net.ketc.numeri.domain.entity

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import net.ketc.numeri.util.ormlite.Entity
import java.io.Serializable

@DatabaseTable
data class TweetsDisplayGroup(@DatabaseField(generatedId = true)
                              override val id: Int = 0) : Entity<Int>

interface Display : Serializable {
    val token: ClientToken
    val group: TweetsDisplayGroup
    var priority: Int
}

@DatabaseTable
data class TimeLineDisplay(
        @DatabaseField(generatedId = true)
        override val id: Int = 0,
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        override val token: ClientToken = ClientToken(),
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        override val group: TweetsDisplayGroup = TweetsDisplayGroup(),
        @DatabaseField(canBeNull = false, dataType = DataType.ENUM_STRING, uniqueCombo = true)
        val type: TimeLineType = TimeLineType.HOME,
        @DatabaseField(canBeNull = false)
        override var priority: Int = 0) : Display, Entity<Int>

fun createTimeLineDisplay(token: ClientToken, group: TweetsDisplayGroup, type: TimeLineType)
        = TimeLineDisplay(token = token, group = group, type = type)

data class TweetsDisplay(
        @DatabaseField(generatedId = true)
        override val id: Int = 0,
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        override val token: ClientToken = ClientToken(),
        @DatabaseField(canBeNull = false, foreign = true, uniqueCombo = true)
        override val group: TweetsDisplayGroup = TweetsDisplayGroup(),
        @DatabaseField(canBeNull = false, uniqueCombo = true)
        val foreignId: Long = 0,
        @DatabaseField(canBeNull = false, uniqueCombo = true)
        val type: TweetsDisplayType = TweetsDisplayType.USER_LIST,
        @DatabaseField(canBeNull = false)
        override var priority: Int = 0) : Display, Entity<Int>

fun createTweetsDisplay(token: ClientToken, group: TweetsDisplayGroup, foreignId: Long, type: TweetsDisplayType)
        = TweetsDisplay(token = token, group = group, foreignId = foreignId, type = type)

enum class TimeLineType {
    HOME, MENTIONS
}

enum class TweetsDisplayType {
    USER_LIST, PUBLIC
}