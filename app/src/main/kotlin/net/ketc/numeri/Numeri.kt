package net.ketc.numeri

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import net.ketc.numeri.domain.entity.entities
import net.ketc.numeri.util.android.SafePostDelegate
import net.ketc.numeri.util.log.v
import net.ketc.numeri.util.ormlite.createTable
import java.util.*
import kotlin.collections.LinkedHashMap


class Numeri : Application() {
    private val mActivityManger = ActivityManagerImpl()
    val activityManager: ActivityManager = mActivityManger

    override fun onCreate() {
        super.onCreate()
        cApplication = this
        registerActivityLifecycleCallbacks(mActivityManger)
        Injectors.test = false
        createTable(*entities)
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlytics)
    }

    companion object {
        private var cApplication: Application? = null
        val application: Application
            get() = cApplication!!
    }
}

val Application.activityManger: ActivityManager
    get() {
        return (this as? Numeri)?.activityManager ?: throw CastFailedError()
    }

class CastFailedError : Error()

interface ActivityManager {
    fun checkStartedForFirst(id: UUID): Boolean
    fun getActivityId(activity: Activity): UUID
    fun getActivity(id: UUID): Activity
    fun getSafePostDelegate(id: UUID): SafePostDelegate
}

class ActivityManagerImpl : Application.ActivityLifecycleCallbacks, ActivityManager {

    private val id2ActivityMap = LinkedHashMap<UUID, Activity?>()
    private val activity2IdMap = LinkedHashMap<Activity, UUID>()
    private val id2SafePostDelegateMap = LinkedHashMap<UUID, SafePostDelegate>()
    private val activity2IsStartedForFirst = LinkedHashMap<UUID, Boolean>()

    /**
     * checks whether the specified activity is the first startup.
     * return true if the process returns after being killed.
     * @param id activity id
     */
    override fun checkStartedForFirst(id: UUID) = activity2IsStartedForFirst.getOrPut(id) { true }

    override fun getActivityId(activity: Activity): UUID {
        val id = activity2IdMap.getOrPut(activity) { UUID.randomUUID() }
        checkStartedForFirst(id)
        id2ActivityMap.put(id, activity)
        return id
    }

    override fun getActivity(id: UUID): Activity = id2ActivityMap[id] ?: throw IllegalArgumentException()

    override fun getSafePostDelegate(id: UUID): SafePostDelegate = id2SafePostDelegateMap.getOrPut(id) { SafePostDelegate() }

    override fun onActivityPaused(activity: Activity) {
        activity2IdMap[activity]?.let { id ->
            v(javaClass.simpleName, "pause ${activity.javaClass.simpleName}$id")
            id2SafePostDelegateMap[id]?.onPause()
        }
    }

    override fun onActivityResumed(activity: Activity) {
        activity2IdMap[activity]?.let { id ->
            id2SafePostDelegateMap[id]?.onResume()
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activity2IdMap[activity]?.let { id ->
            activity2IdMap.remove(activity)
            val tag = activity.javaClass.simpleName
            if (activity.isFinishing) {
                id2SafePostDelegateMap.remove(id)
                id2ActivityMap.remove(id)
                activity2IsStartedForFirst.remove(id)
                v(tag, "onDestroy isFinishing")
            } else {
                activity2IsStartedForFirst.put(id, false)
                id2SafePostDelegateMap[id]!!.onDestroy()
                id2ActivityMap.put(id, null)
                v(tag, "onDestroy isNotFinishing")
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        activity2IdMap[activity]?.let {
            outState.putSerializable(EXTRA_ACTIVITY_ID, it)
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        savedInstanceState?.getSerializable(EXTRA_ACTIVITY_ID)?.let {
            if (it !is UUID) throw IllegalStateException()
            activity2IdMap.put(activity, it)
            id2ActivityMap.put(it, activity)
        } ?: getActivityId(activity)
    }

    companion object {
        val EXTRA_ACTIVITY_ID = "EXTRA_ACTIVITY_ID"
    }
}