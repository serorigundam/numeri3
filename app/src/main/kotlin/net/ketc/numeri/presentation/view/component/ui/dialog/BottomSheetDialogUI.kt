package net.ketc.numeri.presentation.view.component.ui.dialog

import android.content.Context
import android.support.design.widget.BottomSheetDialog
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import net.ketc.numeri.R
import net.ketc.numeri.presentation.view.component.ui.UI
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView

class BottomSheetDialogUI(override val ctx: Context) : UI {
    override fun createView() = ctx.relativeLayout {
        backgroundColor = ctx.getColor(R.color.colorPrimaryDark)

        lparams(matchParent, wrapContent)

        textView {
            id = R.id.message_text
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            textSizeDimen = R.dimen.text_size_medium
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }

        nestedScrollView {
            id = R.id.nested_scroll
            linearLayout {
                id = R.id.menu_linear
                orientation = LinearLayout.VERTICAL
                lparams(matchParent, wrapContent)
            }
        }.lparams(matchParent, wrapContent) {
            below(R.id.message_text)
        }
    }
}

fun BottomSheetDialog.addMenu(view: View) {
    (this.findViewById(net.ketc.numeri.R.id.menu_linear) as ViewGroup).addView(view)
}

val BottomSheetDialog.messageText: TextView
    get() = findViewById(R.id.message_text)!! as TextView