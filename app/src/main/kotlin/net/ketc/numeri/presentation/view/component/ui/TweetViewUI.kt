package net.ketc.numeri.presentation.view.component.ui

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import net.ketc.numeri.R
import org.jetbrains.anko.*

class TweetViewUI(override val ctx: Context) : UI {

    override fun createView(): View = ctx.frameLayout {
        lparams(matchParent, wrapContent)
        relativeLayout {
            id = R.id.overlay_relative
            lparams(matchParent, wrapContent) {
                padding = dimen(R.dimen.margin_small)
            }

            imageView {
                id = R.id.icon_image
                backgroundColor = ctx.getColor(R.color.image_background_transparency)
            }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon)) {
                alignParentLeft()
                below(R.id.sub_info_text)
                marginEnd = dimen(R.dimen.margin_small)
            }

            textView {
                id = R.id.sub_info_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
                visibility = View.GONE
            }.lparams(matchParent, wrapContent) {
                rightOf(R.id.icon_image)
                leftOf(R.id.twitter_bird_image)
                bottomMargin = dimen(R.dimen.margin_text_small)
            }

            imageView {
                id = R.id.twitter_bird_image
                setImageDrawable(ctx.getDrawable(R.drawable.ic_twitter_circle_white))
            }.lparams(dip(16), dip(16)) {
                alignParentTop()
                alignParentEnd()
                bottomMargin = dimen(R.dimen.margin_text_small)
            }

            textView {
                id = R.id.user_name_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
            }.lparams(wrapContent, wrapContent) {
                below(R.id.sub_info_text)
                rightOf(R.id.icon_image)
                leftOf(R.id.created_at_text)
                marginEnd = dimen(R.dimen.margin_text_small)
            }

            textView {
                id = R.id.screen_name_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.user_name_text)
                rightOf(R.id.icon_image)
                leftOf(R.id.created_at_text)
                marginEnd = dimen(R.dimen.margin_text_small)
            }

            textView {
                id = R.id.created_at_text
                lines = 1
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.twitter_bird_image)
                alignParentEnd()
            }

            textView {
                id = R.id.text
            }.lparams(wrapContent, wrapContent) {
                below(R.id.icon_image)
                rightOf(R.id.icon_image)
            }

            thumbnails()

            textView {
                id = R.id.via_text
                text = "via"
            }.lparams(wrapContent, wrapContent) {
                below(R.id.thumbnails_relative)
                topMargin = dimen(R.dimen.margin_text_small)
            }

            textView {
                id = R.id.source_text
            }.lparams(wrapContent, wrapContent) {
                below(R.id.thumbnails_relative)
                rightOf(R.id.via_text)
                topMargin = dimen(R.dimen.margin_text_small)
            }
        }
    }

    private fun _RelativeLayout.thumbnails() {

        fun _RelativeLayout.thumb(id: Int, init: RelativeLayout.LayoutParams.() -> Unit = {}) {
            imageView {
                this.id = id
                backgroundColor = ctx.getColor(R.color.image_background_transparency)
            }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon), init)
        }

        relativeLayout {
            id = R.id.thumbnails_relative
            lparams(matchParent, matchParent) {
                rightOf(R.id.icon_image)
                topMargin = dimen(R.dimen.margin_small)
                below(R.id.text)
            }

            thumb(R.id.thumb_image1)
            thumb(R.id.thumb_image2) {
                rightOf(R.id.thumb_image1)
                marginStart = dimen(R.dimen.margin_small)
            }
            thumb(R.id.thumb_image3) {
                rightOf(R.id.thumb_image2)
                marginStart = dimen(R.dimen.margin_small)
            }
            thumb(R.id.thumb_image4) {
                rightOf(R.id.thumb_image3)
                marginStart = dimen(R.dimen.margin_small)
            }
        }
    }
}

val View.iconImage: ImageView
    get() = find(R.id.icon_image)
val View.subInfoText: TextView
    get() = find(R.id.sub_info_text)
val View.screenNameText: TextView
    get() = find(R.id.screen_name_text)
val View.userNameText: TextView
    get() = find(R.id.user_name_text)
val View.createdAtText: TextView
    get() = find(R.id.created_at_text)
val View.text: TextView
    get() = find(R.id.text)
val View.thumbnails: List<ImageView>
    get() {
        return listOf(find(R.id.thumb_image1), find(R.id.thumb_image2),
                find(R.id.thumb_image3), find(R.id.thumb_image4))
    }

val View.sourceText: TextView
    get() = find(R.id.source_text)
val View.overlayRelative: RelativeLayout
    get() = find(R.id.overlay_relative)