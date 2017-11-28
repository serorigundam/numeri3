package tech.ketc.numeri.util.arch.lifecycle

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

interface IOnActiveRunner {
    fun setOwner(owner: LifecycleOwner)
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

        private var isSafe = true
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
            isSafe = true
            tasks.forEach { it() }
            tasks.clear()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            isSafe = false
        }
    }
}