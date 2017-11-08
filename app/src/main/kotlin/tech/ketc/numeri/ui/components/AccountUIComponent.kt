package tech.ketc.numeri.ui.components

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.android.getResourceId
import tech.ketc.numeri.util.anko.UIComponent

class AccountUIComponent : UIComponent<RelativeLayout> {

    lateinit var screenNameText: TextView
        private set
    lateinit var userNameText: TextView
        private set
    lateinit var iconImage: ImageView
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        lparams(matchParent, wrapContent) {
            padding = dimen(R.dimen.margin_medium)
            gravity = Gravity.CENTER_VERTICAL
        }
        backgroundResource = context.getResourceId(android.R.attr.selectableItemBackground)
        isClickable = true

        imageView {
            iconImage = this
            id = R.id.icon_image
            backgroundColor = ctx.getColor(R.color.image_background_transparency)
        }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon)) {
            marginEnd = dimen(R.dimen.margin_medium)
        }

        textView {
            userNameText = this
            id = R.id.user_name_text
            ellipsize = TextUtils.TruncateAt.END
            lines = 1
            textColor = ctx.getColor(ctx.getResourceId(android.R.attr.textColorPrimary))
            textSizeDimen = R.dimen.text_size_medium
        }.lparams {
            marginEnd = dimen(R.dimen.margin_medium)
            sameTop(R.id.icon_image)
            rightOf(R.id.icon_image)
        }
        textView {
            screenNameText = this
            id = R.id.screen_name_text
            ellipsize = TextUtils.TruncateAt.END
            lines = 1
            textSizeDimen = R.dimen.text_size_medium
        }.lparams {
            marginEnd = dimen(R.dimen.margin_medium)
            sameBottom(R.id.icon_image)
            rightOf(R.id.icon_image)
            below(R.id.user_name_text)
        }
    }
}