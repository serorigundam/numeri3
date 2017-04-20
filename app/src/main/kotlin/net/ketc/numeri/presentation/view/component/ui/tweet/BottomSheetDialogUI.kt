package net.ketc.numeri.presentation.view.component.ui.tweet

import android.content.Context
import android.text.TextUtils
import android.widget.LinearLayout
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