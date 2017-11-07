package tech.ketc.numeri.util.arch.livedata

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData

fun <S, T> LiveData<S?>.asyncSwitchMap(func: suspend (S?) -> T) = MutableAsyncLiveData(this, func)


open class NonnullLiveData<T : Any> : LiveData<T>()


open class NonnullMediatorLiveData<T : Any> : MediatorLiveData<T>()