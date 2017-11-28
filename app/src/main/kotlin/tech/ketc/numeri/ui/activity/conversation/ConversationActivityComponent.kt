package tech.ketc.numeri.ui.activity.conversation

import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
interface ConversationActivityComponent : AndroidInjector<ConversationActivity> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ConversationActivity>()
}