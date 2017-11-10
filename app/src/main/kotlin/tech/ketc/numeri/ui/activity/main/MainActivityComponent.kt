package tech.ketc.numeri.ui.activity.main

import dagger.android.AndroidInjector
import dagger.Subcomponent


@Subcomponent
interface MainActivityComponent : AndroidInjector<MainActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainActivity>()
}