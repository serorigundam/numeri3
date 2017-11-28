package tech.ketc.numeri.ui.model.factory

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.ArrayMap
import tech.ketc.numeri.ui.model.di.ViewModelComponent
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.logTag
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
class ViewModelFactory @Inject constructor(viewModelComponent: ViewModelComponent) : ViewModelProvider.Factory {
    private val mProviders = ArrayMap<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>()

    init {
        Logger.v(logTag, "new model factory")
        put(viewModelComponent::mainViewModel)
        put(viewModelComponent::timeLineViewModel)
        put(viewModelComponent::timelineManageViewModel)
        put(viewModelComponent::tweetViewModel)
        put(viewModelComponent::mediaViewModel)
    }

    private inline fun <reified T : ViewModel> put(noinline provide: () -> T) {
        mProviders.put(T::class.java, Provider(provide))
    }

    override fun <T : ViewModel?> create(viewModelClass: Class<T>): T {
        var provider: Provider<ViewModel>? = mProviders[viewModelClass]
        if (provider == null) {
            for ((key, value) in mProviders) {
                if (viewModelClass.isAssignableFrom(key)) {
                    provider = value
                    break
                }
            }
        }
        return (provider?.get() as T) ?: throw IllegalArgumentException("not exist VewModel " + viewModelClass)
    }
}