package tech.ketc.numeri.ui.activity.media

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface MediaActivityComponent : AndroidInjector<MediaActivity> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<MediaActivity>()
}