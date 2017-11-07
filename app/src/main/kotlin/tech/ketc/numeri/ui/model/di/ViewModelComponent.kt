package tech.ketc.numeri.ui.model.di

import dagger.Subcomponent
import tech.ketc.numeri.ui.model.MainViewModel


@Subcomponent
interface ViewModelComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ViewModelComponent
    }

    fun mainViewModel(): MainViewModel
}