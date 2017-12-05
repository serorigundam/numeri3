package tech.ketc.numeri.ui.components

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.anko.marginBottom
import tech.ketc.numeri.util.anko.marginTop

class UserUIComponent : IUserUIComponent {
    override lateinit var componentRoot: RelativeLayout
        private set
    override lateinit var screenNameText: TextView
        private set
    override lateinit var userNameText: TextView
        private set
    override lateinit var descriptionText: TextView
        private set
    override lateinit var followButton: ImageButton
        private set
    override lateinit var iconImage: ImageView
        private set
    override lateinit var iconBack: View
        private set

    override fun createView(ctx: Context) = ctx.relativeLayout {
        componentRoot = this
        frameLayout {
            id = R.id.icon_image
            iconBack = this
            backgroundColor = ctx.getColor(R.color.image_background_transparency)
            imageView {
                iconImage = this
                backgroundColor = ctx.getColor(R.color.transparent)
            }
        }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon)) {
            marginTop = dimen(R.dimen.margin_medium)
            marginStart = dimen(R.dimen.margin_medium)
            marginEnd = dimen(R.dimen.margin_small)
            alignParentStart()
            alignParentTop()
        }

        imageButton {
            followButton = this
            id = R.id.follow_button
            backgroundDrawable = ctx.getDrawable(R.drawable.ripple_frame)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }.lparams(dip(56), dip(32)) {
            alignParentTop()
            alignParentEnd()
            marginTop = dimen(R.dimen.margin_medium)
            marginEnd = dimen(R.dimen.margin_medium)
            marginStart = dimen(R.dimen.margin_small)
        }

        textView {
            userNameText = this
            id = R.id.user_name_text
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
        }.lparams(wrapContent, wrapContent) {
            endOf(R.id.icon_image)
            startOf(R.id.follow_button)
            marginTop = dimen(R.dimen.margin_medium)
            marginEnd = dimen(R.dimen.margin_medium)
            marginBottom = dimen(R.dimen.margin_text_small)
        }

        textView {
            screenNameText = this
            ellipsize = TextUtils.TruncateAt.END
            maxLines = 1
        }.lparams(wrapContent, wrapContent) {
            endOf(R.id.icon_image)
            startOf(R.id.follow_button)
            below(R.id.user_name_text)
            marginEnd = dimen(R.dimen.margin_medium)
        }

        textView {
            descriptionText = this
        }.lparams(wrapContent, wrapContent) {
            endOf(R.id.icon_image)
            below(R.id.icon_image)
            marginTop = dimen(R.dimen.margin_text_small)
            marginBottom = dimen(R.dimen.margin_small)
        }
    }
}