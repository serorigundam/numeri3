package tech.ketc.numeri.ui.fragment.main

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface MainFragmentComponent : AndroidInjector<MainFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MainFragment>()
}