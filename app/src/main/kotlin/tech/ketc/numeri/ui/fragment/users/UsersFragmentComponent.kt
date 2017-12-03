package tech.ketc.numeri.ui.fragment.users

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface UsersFragmentComponent : AndroidInjector<UsersFragment> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<UsersFragment>()
}