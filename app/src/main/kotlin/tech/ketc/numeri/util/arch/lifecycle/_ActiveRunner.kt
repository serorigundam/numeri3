package tech.ketc.numeri.util.arch.lifecycle

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent

interface IOnActiveRunner {
    /**
     * @param owner
     */
    fun setOwner(owner: LifecycleOwner)

    /**
     * @param handle called when the [OnLifecycleEvent] onResume()
     */
    fun runOnActive(handle: () -> Unit)
}

class OnActiveRunner : IOnActiveRunner {
    private val mObserver = OnActiveLifeCycleObserver()
    private var mIsOwnerInitialized = false

    override fun setOwner(owner: LifecycleOwner) {
        if (!mIsOwnerInitialized)
            owner.lifecycle.addObserver(mObserver)
        else throw IllegalStateException()
        mIsOwnerInitialized = true
    }

    override fun runOnActive(handle: () -> Unit) {
        if (!mIsOwnerInitialized) throw IllegalStateException()
        mObserver.run(handle)
    }

    class OnActiveLifeCycleObserver : LifecycleObserver {

        private var isSafe = false
        private val tasks = ArrayList<() -> Unit>()

        fun run(task: () -> Unit) {
            if (isSafe) {
                task()
            } else {
                tasks.add(task)
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            isSafe = false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            tasks.forEach { it() }
            tasks.clear()
            isSafe = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            isSafe = false
        }
    }
}