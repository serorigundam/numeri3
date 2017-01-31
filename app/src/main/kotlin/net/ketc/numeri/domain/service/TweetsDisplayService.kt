package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.util.copy
import net.ketc.numeri.util.ormlite.Transaction
import net.ketc.numeri.util.ormlite.delete
import net.ketc.numeri.util.ormlite.transaction
import net.ketc.numeri.util.toImmutableList
import java.util.*

interface TweetsDisplayService {

    fun createGroup(): TweetsDisplayGroup

    fun createDisplay(group: TweetsDisplayGroup, twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType): TweetsDisplay

    fun removeGroup(group: TweetsDisplayGroup)

    fun remove(tweetsDisplay: TweetsDisplay)

    fun getAllGroup(): List<TweetsDisplayGroup>

    fun getDisplays(group: TweetsDisplayGroup): List<TweetsDisplay>

    fun replace(to: TweetsDisplay, by: TweetsDisplay)
}

class TweetsDisplayServiceImpl : TweetsDisplayService {
    private val displaysMap = LinkedHashMap<TweetsDisplayGroup, MutableList<TweetsDisplay>>()

    init {
        transaction {
            val groupDao = dao(TweetsDisplayGroup::class)
            val groups = groupDao.queryForAll().toImmutableList()
            groups.forEach {
                val tweetsDisplayDao = dao(TweetsDisplay::class)
                val tweetsDisplayList = tweetsDisplayDao.queryBuilder()
                        .orderBy("order", true)
                        .query()
                displaysMap.put(it, tweetsDisplayList)
            }
            displaysMap.map { it.key }.toImmutableList()
        }
    }

    override fun createGroup() = transaction {
        val dao = dao(TweetsDisplayGroup::class)
        val group = TweetsDisplayGroup()
        dao.create(group)
        displaysMap.put(group, ArrayList())
        return@transaction group
    }

    override fun createDisplay(group: TweetsDisplayGroup, twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType): TweetsDisplay = transaction {
        checkExistence(group)
        val display = createTweetsDisplay(twitterClient.toClientToken(), group, foreignId, type)
        val dao = dao(TweetsDisplay::class)
        display.order = dao.count { it.group.id == group.id }
        displaysMap[group]!!.add(display)
        dao.create(display)
        display
    }

    override fun removeGroup(group: TweetsDisplayGroup): Unit = transaction {
        displaysMap[group] ?: throw GroupDoesNotExistWasSpecifiedException()
        val displayDao = dao(TweetsDisplay::class)
        displayDao.delete { eq("group_id", group.id) }
        val groupDao = dao(TweetsDisplayGroup::class)
        groupDao.delete(group)
        displaysMap.remove(group)
    }

    override fun remove(tweetsDisplay: TweetsDisplay): Unit = transaction {
        val displays = displaysMap[tweetsDisplay.group] ?: throw  GroupDoesNotExistWasSpecifiedException()
        if (!displays.remove(tweetsDisplay)) {
            throw DisplayDoesNotExistWasSpecifiedException()
        }
        val deleteDisplayOrder = tweetsDisplay.order
        val updatedDisplay = displays.filter { it.order > deleteDisplayOrder }
                .mapIndexed { i, tweetsDisplay -> tweetsDisplay.apply { order = deleteDisplayOrder + i } }
        val dao = dao(TweetsDisplay::class)
        updatedDisplay.forEach { dao.update(it) }
        dao.delete(tweetsDisplay)
    }

    override fun getAllGroup(): List<TweetsDisplayGroup> {
        return displaysMap.map { it.key }
    }

    override fun getDisplays(group: TweetsDisplayGroup): List<TweetsDisplay> {
        return displaysMap[group]?.copy() ?: throw GroupDoesNotExistWasSpecifiedException()
    }

    override fun replace(to: TweetsDisplay, by: TweetsDisplay): Unit = transaction {
        if (to.group.id != by.group.id)
            throw IllegalArgumentException()
        val temp = to.order
        to.order = by.order
        by.order = temp
        val displays = displaysMap[to.group] ?: throw GroupDoesNotExistWasSpecifiedException()
        displays.first { it.id == to.id }.order = to.order
        displays.first { it.id == by.id }.order = by.order
        displays.sortBy { it.order }
        val dao = dao(TweetsDisplay::class)
        dao.update(to)
        dao.update(by)
    }

    private fun Transaction.checkExistence(group: TweetsDisplayGroup) {
        val groupDao = dao(TweetsDisplayGroup::class)
        groupDao.queryForId(group.id) ?: throw GroupDoesNotExistWasSpecifiedException()
    }
}

class GroupDoesNotExistWasSpecifiedException : IllegalArgumentException("group that does not exist was specified")
class DisplayDoesNotExistWasSpecifiedException : IllegalArgumentException("display that does not exist was specified")