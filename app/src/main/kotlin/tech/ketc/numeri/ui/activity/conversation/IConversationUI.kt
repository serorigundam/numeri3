package tech.ketc.numeri.ui.activity.conversation

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import org.jetbrains.anko.AnkoComponent

interface IConversationUI : AnkoComponent<ConversationActivity> {
    val toolbar: Toolbar
    val recycler: RecyclerView
}