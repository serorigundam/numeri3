package tech.ketc.numeri.util.arch

import android.arch.lifecycle.*

interface BindingLifecycleStream<out T> {
    fun observe(owner: LifecycleOwner, handle: (T) -> Unit)
}