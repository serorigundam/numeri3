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

    private fun initialize() = mLock.read {
        if (mIsInitialized) return
        Logger.v(logTag, "initialize()")
        val groupList = mGroupDao.selectAll()
        groupList.forEach {
            val groupName = it.name
            val infoList = mInfoDao.selectByGroupName(groupName)
            mGroupToInfoListMap.put(groupName, infoList.toMutableList())
        }
        mIsInitialized = true
    }

    override fun createGroup(groupName: String): TimelineGroup {
        initialize()
        val list = mGroupToInfoListMap[groupName]
        if (list != null) throw AlreadyExistsException("TimelineGroup", "groupName[$groupName]")
        val group = TimelineGroup(groupName)
        mGroupToInfoListMap.put(groupName, ArrayList())
        mGroupDao.insert(group)
        return group
    }

    override fun joinToGroup(group: TimelineGroup, info: TimelineInfo) {
        initialize()
        mGroupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "loadGroupList[${group.name}]")
        val count = mInfoDao.countInfoByGroup(group.name)
        mInfoDao.createOrUpdateGroupToInfo(TlGroupToTlInfo(group.name, info.id, count))
        mGroupToInfoListMap[group.name]!!.add(info)
    }

    override fun selectByGroup(group: TimelineGroup): List<TimelineInfo> {
        initialize()
        return mGroupToInfoListMap[group.name] ?: throw NotExistsException("TimelineGroup", "loadGroupList[${group.name}]")
    }

    override fun getInfo(type: TlType, accountId: Long, foreignId: Long): TimelineInfo {
        mInfoDao.selectAll().forEach {
            Logger.v(javaClass.name, "${it.id} ${it.type.name} ${it.accountId} ${it.foreignId}")
        }
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
        return mGroupToInfoListMap.keys.map { TimelineGroup(it) }.unmodifiableList()
    }

    override fun notifyDataChanged() {
        mEmptyLiveData.postValue(Any())
    }

    override fun observe(owner: LifecycleOwner, handle: () -> Unit) {
        mEmptyLiveData.observe(owner) { handle() }
    }
}