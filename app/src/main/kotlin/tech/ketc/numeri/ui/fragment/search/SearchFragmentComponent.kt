package tech.ketc.numeri.ui.fragment.search

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface SearchFragmentComponent : AndroidInjector<SearchFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<SearchFragment>()
}