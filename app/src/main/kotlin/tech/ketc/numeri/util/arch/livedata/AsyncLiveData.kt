package tech.ketc.numeri.util.arch.livedata


import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.response.Response
import tech.ketc.numeri.util.arch.response.deferredRes
import tech.ketc.numeri.util.coroutine.asyncContext

open class AsyncLiveData<T : Any>(private val mTask: suspend () -> T)
    : NonnullLiveData<Response<T>>(), Cancellable {


    private var mJob: Job? = null

    fun observe(owner: LifecycleOwner, onChanged: (Response<T>) -> Unit) {
        super.observe(owner, Observer { onChanged(it ?: throw IllegalStateException()) })
    }

    override fun onActive() {
        super.onActive()
        if (mJob == null) {
            mJob = launch(UI) {
                setValue(deferredRes(asyncContext) { mTask() }.await())
            }
        }
    }

    override fun cancel() {
        mJob?.cancel()
    }
}