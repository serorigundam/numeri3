package tech.ketc.numeri.util.arch.livedata

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import tech.ketc.numeri.util.arch.response.Response
import tech.ketc.numeri.util.arch.response.deferredRes
import tech.ketc.numeri.util.coroutine.asyncContext

class MutableAsyncLiveData<S, T : Any>(trigger: LiveData<S?>, private val mTransform: suspend (S?) -> T)
    : NonnullMediatorLiveData<Response<T>>(), Cancellable {

    private var mJob: Job? = null

    init {
        addSource(trigger, { asyncTransform(it) })
    }

    fun observe(owner: LifecycleOwner, onChanged: (Response<T>) -> Unit) {
        super.observe(owner, Observer { onChanged(it ?: throw IllegalStateException()) })
    }

    @SuppressWarnings
    override fun setValue(value: Response<T>) {
        super.setValue(value)
    }

    @SuppressWarnings
    override fun postValue(value: Response<T>) {
        super.postValue(value)
    }

    private fun asyncTransform(source: S?) {
        mJob?.cancel()
        mJob = null
        mJob = launch(UI) {
            setValue(deferredRes(asyncContext) { mTransform(source) }.await())
        }
    }

    override fun cancel() {
        mJob?.cancel()
    }
}