package tech.ketc.numeri.domain.repository

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.util.ArrayMap
import tech.ketc.numeri.infra.AccountDatabase
import tech.ketc.numeri.infra.element.TlType
import tech.ketc.numeri.infra.entity.TimelineGroup
import tech.ketc.numeri.infra.entity.TimelineInfo
import tech.ketc.numeri.infra.entity.TlGroupToTlInfo
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.livedata.observe
import tech.ketc.numeri.util.logTag
import tech.ketc.numeri.util.unmodifiableList
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read

class TimelineInfoRepository @Inject constructor(private val mDatabase: AccountDatabase) : ITimelineInfoRepository {
    private val mGroupDao = mDatabase.timeLineGroupDao()
    private val mInfoDao = mDatabase.timeLineInfoDao()
    private val mGroupToInfoListMap = ArrayMap<String, MutableList<TimelineInfo>>()
    private var mIsInitialized = false
    private var mLock = ReentrantReadWriteLock()
    private val mEmptyLiveData = MutableLiveData<Any>()
    private val mGroupList = ArrayList<TimelineGroup>()

    private fun initialize() = mLock.read {
        if (mIsInitialized) return
        Logger.v(logTag, "initialize()")
        val groupList = mGroupDao.selectAll()
        groupList.forEach {
            mGroupList.add(it)
            val groupName = it.name
            val infoList = mInfoDao.selectByGroupName(groupName)
            mGroupToInfoListMap.put(groupName, infoList.toMutableList())
            Logger.v(logTag, groupName)
        }
        mIsInitialized = true
    }

    override fun createGroup(groupName: String): TimelineGroup {
        initialize()
        val list = mGroupToInfoListMap[groupName]
        if (list != null) throw AlreadyExistsException("TimelineGroup", "groupName[$groupName]")
        val group = TimelineGroup(groupName)
        mGroupDao.insert(group)
        mGroupToInfoListMap.put(groupName, ArrayList())
        mGroupList.add(TimelineGroup(groupName))
        return group
    }

    override fun joinToGroup(group: TimelineGroup, info: TimelineInfo) {
        initialize()
        mGroupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "group[${group.name}]")
        val count = mInfoDao.countInfoByGroupName(group.name)
        Logger.v(logTag, "join to ${group.name} position $count")
        mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(group.name, info.id, count))
        mGroupToInfoListMap[group.name]!!.add(info)
    }

    override fun removeFromGroup(group: TimelineGroup, info: TimelineInfo) {
        initialize()
        val list = mGroupToInfoListMap[group.name]
        list ?: throw NotExistsException("TimelineGroup", "group[${group.name}]")
        val order = mInfoDao.checkOrder(group.name, info.id) ?: throw NotExistsException("TimelineInfo", "group[${group.name}] info[${info.id}]")
        Logger.v(logTag, "delete from ${group.name} position $order")
        mInfoDao.selectByGroupGreaterThan(group.name, order).forEachIndexed { i, value ->
            Logger.v(logTag, "delete from ${group.name} modify position ${order + i}")
            mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(group.name, value.id, order + i))
        }
        mInfoDao.deleteRelationByGroupName(info.id, group.name)
        list.removeAt(list.indexOfFirst { it.id == info.id })
    }

    override fun selectByGroup(group: TimelineGroup): List<TimelineInfo> {
        initialize()
        return (mGroupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "loadGroupList[${group.name}]")).unmodifiableList()
    }

    override fun getInfo(type: TlType, accountId: Long, foreignId: Long): TimelineInfo {
        fun insert(): TimelineInfo {
            mInfoDao.insert(TimelineInfo(type = type,
                    accountId = accountId,
                    foreignId = foreignId))
            return mInfoDao.select(type, accountId, foreignId)!!
        }
        return mInfoDao.select(type, accountId, foreignId) ?: insert()
    }

    override fun getGroupList(): List<TimelineGroup> {
        initialize()
        return mGroupList.unmodifiableList()
    }

    override fun deleteGroup(vararg group: TimelineGroup) {
        val list = group
                .map { g ->
                    mGroupList.find { it.name == g.name }
                            ?: throw NotExistsException("TimelineGroup", "group[${g.name}]")
                }
        list.forEach { g ->
            mGroupToInfoListMap[g.name]!!.forEach { info ->
                mInfoDao.deleteRelationByGroupName(info.id, g.name)
            }
        }
        mGroupDao.deleteAll(*list.toTypedArray())
        list.forEach {
            mGroupList.remove(it)
            mGroupToInfoListMap.remove(it.name)
        }
    }

    override fun notifyDataChanged() {
        mEmptyLiveData.postValue(Any())
    }

    override fun replace(group: TimelineGroup, from: TimelineInfo, to: TimelineInfo) {
        initialize()
        val groupName = group.name
        val list = mGroupToInfoListMap[groupName] ?: throw NotExistsException("TimelineGroup", "group[$groupName]")
        val fromId = from.id
        val fromOrder = mInfoDao.checkOrder(groupName, fromId)
                ?: throw NotExistsException("TimelineInfo", "group[$groupName] fromInfo[$fromId]")
        val toId = to.id
        val toOrder = mInfoDao.checkOrder(groupName, toId)
                ?: throw NotExistsException("TimelineInfo", "group[$groupName] toInfo[$toId]")
        mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(groupName, fromId, toOrder))
        mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(groupName, toId, fromOrder))
        list[toOrder] = from
        list[fromOrder] = to
    }

    override fun insert(group: TimelineGroup, info: TimelineInfo, order: Int) {
        initialize()
        val groupName = group.name
        val list = mGroupToInfoListMap[groupName] ?: throw NotExistsException("TimelineGroup", "group[$groupName]")
        Logger.v(logTag, "insert to  ${group.name}  position $order")
        mInfoDao.selectByGroupGreaterThan(group.name, order).forEachIndexed { i, value ->
            Logger.v(logTag, "insert to  ${group.name}  modify position ${order + i + 1}")
            mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(group.name, value.id, order + i + 1))
        }
        mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(groupName, info.id, order))
        list.indexOfFirst { it.id == info.id }.takeIf { it != -1 }?.let {
            list.removeAt(it)
        }
        list.add(order, info)
    }

    override fun observe(owner: LifecycleOwner, handle: () -> Unit) {
        mEmptyLiveData.observe(owner) { handle() }
    }
}