package net.ketc.numeri.domain.service

import net.ketc.numeri.domain.entity.*
import net.ketc.numeri.util.ormlite.Transaction
import net.ketc.numeri.util.ormlite.delete
import net.ketc.numeri.util.ormlite.transaction
import net.ketc.numeri.util.toImmutableList
import java.util.*

interface TweetsDisplayService {

    fun createGroup(twitterClient: TwitterClient, type: TimeLineType)

    fun createGroup(twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType)

    fun addToGroup(group: TweetsDisplayGroup, twitterClient: TwitterClient, type: TimeLineType)

    fun addToGroup(group: TweetsDisplayGroup, twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType)

    fun removeGroup(groupId: Int)

    fun remove(tweetsDisplay: TweetsDisplay)

    fun remove(timeLineDisplay: TimeLineDisplay)

    fun getAllDisplays(): List<List<Display>>
}

class TweetsDisplayServiceImpl : TweetsDisplayService {
    private val displayGroupList = ArrayList<ArrayList<Display>>()

    override fun createGroup(twitterClient: TwitterClient, type: TimeLineType): Unit = transaction {
        val dao = dao(TweetsDisplayGroup::class)
        val group = TweetsDisplayGroup()
        dao.create(group)
        addToGroup(group, twitterClient, type)
    }

    override fun createGroup(twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType): Unit = transaction {
        val dao = dao(TweetsDisplayGroup::class)
        val group = TweetsDisplayGroup()
        dao.create(group)
        addToGroup(group, twitterClient, foreignId, type)
    }

    override fun addToGroup(group: TweetsDisplayGroup, twitterClient: TwitterClient, type: TimeLineType): Unit = transaction {
        checkExistence(group)
        val display = createTimeLineDisplay(twitterClient.toClientToken(), group, type)
        dao(TimeLineDisplay::class).create(display)
        addTo(display)
    }

    override fun addToGroup(group: TweetsDisplayGroup, twitterClient: TwitterClient, foreignId: Long, type: TweetsDisplayType): Unit = transaction {
        checkExistence(group)
        val display = createTweetsDisplay(twitterClient.toClientToken(), group, foreignId, type)
        dao(TweetsDisplay::class).create(display)
        addTo(display)
    }

    private fun addTo(display: Display) {
        displayGroupList.forEach {
            if (it.single().group.id == display.group.id)
                it.add(display)
        }
    }

    override fun removeGroup(groupId: Int): Unit = transaction {
        val groupDao = dao(TweetsDisplayGroup::class)
        groupDao.queryForId(groupId) ?: throw GroupDoesNotExistEasSpecifiedException()
        val timeLineDisplayDao = dao(TimeLineDisplay::class)
        timeLineDisplayDao.delete { eq("group", groupId) }
        val tweetsDisplayDao = dao(TweetsDisplayGroup::class)
        tweetsDisplayDao.delete { eq("group", groupId) }
        displayGroupList.forEach {
            it.removeAll { it.group.id == groupId }
        }
        displayGroupList.removeAll { it.isEmpty() }
    }

    override fun remove(tweetsDisplay: TweetsDisplay): Unit = transaction {
        dao(TweetsDisplay::class).delete(tweetsDisplay)
        displayGroupList.forEach {
            it.removeAll { it is TweetsDisplay && it.id == tweetsDisplay.id }
        }

        removeGroupIf(tweetsDisplay)
    }

    private fun Transaction.removeGroupIf(display: Display) {
        displayGroupList.filter { it.isEmpty() }.singleOrNull()?.let {
            displayGroupList.flatMap { it }
                    .distinctBy { it.group.id }
                    .singleOrNull { it.group.id == display.group.id }?.let {
                dao(TweetsDisplayGroup::class).deleteById(display.group.id)
            }
        }
    }

    override fun remove(timeLineDisplay: TimeLineDisplay): Unit = transaction {
        dao(TimeLineDisplay::class).delete(timeLineDisplay)
        displayGroupList.forEach {
            it.removeAll { it is TimeLineDisplay && it.id == timeLineDisplay.id }
        }
        removeGroupIf(timeLineDisplay)
    }

    override fun getAllDisplays(): List<List<Display>> = transaction {
        if (displayGroupList.isNotEmpty()) {
            return@transaction displayGroupList.map { it.toImmutableList() }.toImmutableList()
        }
        val dao = dao(TweetsDisplayGroup::class)
        val displayGroupList = ArrayList<ArrayList<Display>>()
        dao.queryForAll().forEach {
            val displayList = ArrayList<Display>()
            displayList.addAll(dao(TweetsDisplay::class).queryForAll())
            displayList.addAll(dao(TimeLineDisplay::class).queryForAll())
            displayGroupList.add(displayList)
        }
        this@TweetsDisplayServiceImpl.displayGroupList.addAll(displayGroupList)
        this@TweetsDisplayServiceImpl.displayGroupList.map { it.toImmutableList() }.toImmutableList()
    }

    private fun Transaction.checkExistence(group: TweetsDisplayGroup) {
        val groupDao = dao(TweetsDisplayGroup::class)
        groupDao.queryForId(group.id) ?: throw GroupDoesNotExistEasSpecifiedException()
    }
}

class GroupDoesNotExistEasSpecifiedException : IllegalArgumentException("group that does not exist was specified")