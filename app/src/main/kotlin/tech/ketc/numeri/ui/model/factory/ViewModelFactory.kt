package tech.ketc.numeri.ui.model.factory

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.util.ArrayMap
import tech.ketc.numeri.ui.model.MainViewModel
import tech.ketc.numeri.ui.model.di.ViewModelComponent
import javax.inject.Inject
import javax.inject.Provider

@Suppress("UNCHECKED_CAST")
class ViewModelFactory @Inject constructor(viewModelComponent: ViewModelComponent) : ViewModelProvider.Factory {
    private val providers = ArrayMap<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>()

    init {
        providers.put(MainViewModel::class.java, Provider(viewModelComponent::mainViewModel))
    }

    override fun <T : ViewModel?> create(viewModelClass: Class<T>): T {
        var provider: Provider<ViewModel>? = providers[viewModelClass]
        if (provider == null) {
            for ((key, value) in providers) {
                if (viewModelClass.isAssignableFrom(key)) {
                    provider = value
                    break
                }
            }
        }
        return (provider?.get() as T) ?: throw IllegalArgumentException("not exist VewModel " + viewModelClass)
    }
}