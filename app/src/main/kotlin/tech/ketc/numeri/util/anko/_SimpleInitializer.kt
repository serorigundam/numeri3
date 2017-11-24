package tech.ketc.numeri.util.anko

import android.widget.TextView
import org.jetbrains.anko.lines
import org.jetbrains.anko.textColor
import tech.ketc.numeri.util.android.getResourceId

val initialize1Line: TextView.() -> Unit = {
    ellipsize = android.text.TextUtils.TruncateAt.END
    lines = 1
    textColor = context.getColor(context.getResourceId(android.R.attr.textColorPrimary))
}