package net.ketc.numeri.presentation.view.component.ui

import android.content.Context
import android.view.View

interface UI {
    val ctx: Context
    fun createView(): View
}