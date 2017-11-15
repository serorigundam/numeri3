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
import tech.ketc.numeri.util.unmodifiableList
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read

class TimelineInfoRepository @Inject constructor(private val db: AccountDatabase) : ITimelineInfoRepository {
    private val groupDao = db.timeLineGroupDao()
    private val infoDao = db.timeLineInfoDao()
    private val groupToInfoListMap = ArrayMap<String, MutableList<TimelineInfo>>()
    private var isInitialized = false
    private var lock = ReentrantReadWriteLock()
    private val mEmptyLiveData = MutableLiveData<Int>()
    private var change = 1

    private fun changeData(): Int {
        change = if (change == 1) {
            2
        } else {
            1
        }
        return change
    }

    private fun initialize() = lock.read {
        if (isInitialized) return
        val groupList = groupDao.selectAll()
        groupList.forEach {
            val groupName = it.name
            val infoList = infoDao.selectByGroupName(groupName)
            groupToInfoListMap.put(groupName, infoList.toMutableList())
        }
        isInitialized = true
    }

    override fun createGroup(groupName: String): TimelineGroup {
        initialize()
        val list = groupToInfoListMap[groupName]
        if (list != null) throw AlreadyExistsException("TimelineGroup", "groupName[$groupName]")
        val group = TimelineGroup(groupName)
        groupToInfoListMap.put(groupName, ArrayList())
        groupDao.insert(group)
        return group
    }

    override fun joinToGroup(group: TimelineGroup, info: TimelineInfo) {
        initialize()
        groupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "groupList[${group.name}]")
        val count = infoDao.countInfoByGroup(group.name)
        infoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(group.name, info.id, count))
        groupToInfoListMap[group.name]!!.add(info)
    }

    override fun selectByGroup(group: TimelineGroup): List<TimelineInfo> {
        initialize()
        return groupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "groupList[${group.name}]")
    }

    override fun getInfo(type: TlType, accountId: Long, foreignId: Long): TimelineInfo {
        infoDao.selectAll().forEach {
            Logger.v(javaClass.name, "${it.id} ${it.type.name} ${it.accountId} ${it.foreignId}")
        }
        fun insert(): TimelineInfo {
            infoDao.insert(TimelineInfo(type = type,
                    accountId = accountId,
                    foreignId = foreignId))
            return infoDao.select(type, accountId, foreignId)!!
        }
        return infoDao.select(type, accountId, foreignId) ?: insert()
    }

    override fun getGroupList(): List<TimelineGroup> {
        initialize()
        return groupToInfoListMap.keys.map { TimelineGroup(it) }.unmodifiableList()
    }

    override fun notifyDataChanged() {
        mEmptyLiveData.postValue(changeData())
    }

    override fun observe(owner: LifecycleOwner, handle: () -> Unit) {
        mEmptyLiveData.observe(owner) { handle() }
    }
}