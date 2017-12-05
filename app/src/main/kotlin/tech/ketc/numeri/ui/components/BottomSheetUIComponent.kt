package tech.ketc.numeri.ui.components

import android.content.Context
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.nestedScrollView
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId

class BottomSheetUIComponent : IBottomSheetUIComponent {
    override lateinit var componentRoot: RelativeLayout
    override lateinit var messageTextView: TextView
    override lateinit var content: ViewGroup
    override fun createView(ctx: Context) = ctx.relativeLayout {
        componentRoot = this
        lparams(matchParent, wrapContent)
        backgroundColor = ctx.getColor(R.color.colorPrimaryDark)

        textView {
            messageTextView = this
            id = R.id.message_text
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            textSizeDimen = R.dimen.text_size_medium
            textColor = ctx.getColor(ctx.getResourceId(android.R.attr.textColorPrimary))
            text = ctx.getString(R.string.select_timeline_group)
        }.lparams(matchParent, wrapContent) {
            margin = dimen(R.dimen.margin_medium)
        }

        nestedScrollView {
            linearLayout {
                content = this
                id = R.id.content
                orientation = LinearLayout.VERTICAL
            }.lparams(matchParent, wrapContent)
        }.lparams(matchParent, wrapContent) {
            below(R.id.message_text)
        }
    }
}