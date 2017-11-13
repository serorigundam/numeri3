package tech.ketc.numeri.util.android.ui

import android.view.View
import org.jetbrains.anko.backgroundResource
import tech.ketc.numeri.util.android.getResourceId

fun View.enableRippleEffect() {
    backgroundResource = context.getResourceId(android.R.attr.selectableItemBackground)
    isClickable = true
}
