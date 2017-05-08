package net.ketc.numeri.presentation.view.component.ui.footer

import android.content.Context
import android.view.View
import net.ketc.numeri.presentation.view.component.ui.UI
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent

class EmptyViewUI(override val ctx: Context) : UI {
    override fun createView(): View = ctx.frameLayout {
        lparams(matchParent, dip(104))
    }
}