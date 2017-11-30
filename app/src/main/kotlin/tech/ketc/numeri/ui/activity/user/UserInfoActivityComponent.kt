package tech.ketc.numeri.ui.activity.user

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface UserInfoActivityComponent : AndroidInjector<UserInfoActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<UserInfoActivity>()
}