package tech.ketc.numeri.util.arch

import android.arch.lifecycle.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.response.Response
import tech.ketc.numeri.util.coroutine.asyncContext
import java.lang.ref.WeakReference

class BindingLifecycleAsyncTask<out T : Any>(private val mTask: suspend () -> T) {

    private val mExecutor = TaskExecutor<T>()
    private var mIsExecuted = false
    private var mOwner: WeakReference<LifecycleOwner>? = null

    fun run(owner: LifecycleOwner, handle: (Response<T>) -> Unit) {
        if (mIsExecuted) throw IllegalStateException()
        mIsExecuted = true
        mOwner = WeakReference(owner)
        owner.lifecycle.addObserver(mExecutor)
        mExecutor.execute(mTask) {
            handle(it)
            owner.lifecycle.removeObserver(mExecutor)
            mOwner = null
        }
    }

    fun cancel() {
        if (!mIsExecuted) throw IllegalStateException()
        val ownerRef = mOwner ?: return
        val owner = ownerRef.get() ?: return
        owner.lifecycle.removeObserver(mExecutor.also { it.cancel() })
        mOwner = null
    }

    class TaskExecutor<T : Any> : LifecycleObserver {
        private var isPropagatable = true
        private var isDestroy = false
        private var job: Job? = null

        private val callbacks = ArrayList<(Response<T>) -> Unit>()

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            isPropagatable = true
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            isPropagatable = false
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            isDestroy = true
        }

        fun execute(task: suspend () -> T, callback: (Response<T>) -> Unit) {
            job = launch(UI) {
                val res = async(asyncContext) {
                    var result: T? = null
                    var error: Throwable? = null
                    try {
                        result = task()
                    } catch (t: Throwable) {
                        error = t
                    }
                    object : Response<T> {
                        override val result: T
                            get() = result ?: throw IllegalStateException()
                        override val error: Throwable
                            get() = error ?: throw IllegalStateException()
                        override val isSuccessful: Boolean
                            get() = result != null
                    }
                }.await()
                if (isDestroy) return@launch
                if (isPropagatable) callback(res)
                else callbacks.add(callback)
            }
        }

        fun cancel() {
            job?.cancel()
        }
    }
}