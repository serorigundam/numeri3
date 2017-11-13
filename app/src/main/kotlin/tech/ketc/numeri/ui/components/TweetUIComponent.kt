package tech.ketc.numeri.ui.components

import android.content.Context
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.jetbrains.anko.*
import tech.ketc.numeri.R
import tech.ketc.numeri.util.anko.endOf
import tech.ketc.numeri.util.anko.startOf
import tech.ketc.numeri.util.unmodifiableList

class TweetUIComponent : ITweetUIComponent {

    override lateinit var iconImage: ImageView
        private set
    override lateinit var subInfoIcon: ImageView
        private set
    override lateinit var subInfoText: TextView
        private set
    override lateinit var screenNameText: TextView
        private set
    override lateinit var userNameText: TextView
        private set
    override lateinit var createdAtText: TextView
        private set
    override lateinit var text: TextView
        private set
    override lateinit var sourceText: TextView
        private set
    override lateinit var overlayRelative: RelativeLayout
        private set

    override val thumbnails: List<ImageView>
        get() = mThumbnails.unmodifiableList()

    private val mThumbnails = ArrayList<ImageView>()

    override fun createView(ctx: Context) = ctx.frameLayout {
        lparams(matchParent, wrapContent)
        relativeLayout {
            overlayRelative = this
            id = R.id.overlay_relative
            lparams(matchParent, wrapContent) {
                padding = dimen(R.dimen.margin_small)
            }

            frameLayout {
                backgroundColor = ctx.getColor(R.color.image_background_transparency)
                id = R.id.icon_image
                imageView {
                    iconImage = this
                    backgroundColor = ctx.getColor(R.color.transparent)
                }
            }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon)) {
                alignParentLeft()
                below(R.id.sub_info_text)
                marginEnd = dimen(R.dimen.margin_small)
            }

            imageView {
                subInfoIcon = this
                id = R.id.sub_info_icon
            }.lparams(dip(16), dip(16)) {
                alignEnd(R.id.icon_image)
                bottomMargin = dimen(R.dimen.margin_text_small)
            }


            textView {
                subInfoText = this
                id = R.id.sub_info_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
                visibility = View.GONE
            }.lparams(matchParent, wrapContent) {
                endOf(R.id.icon_image)
                startOf(R.id.twitter_bird_image)
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
                userNameText = this
                id = R.id.user_name_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
            }.lparams(wrapContent, wrapContent) {
                below(R.id.sub_info_text)
                endOf(R.id.icon_image)
                startOf(R.id.created_at_text)
                marginEnd = dimen(R.dimen.margin_text_small)
            }

            textView {
                screenNameText = this
                id = R.id.screen_name_text
                lines = 1
                ellipsize = TextUtils.TruncateAt.END
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.user_name_text)
                endOf(R.id.icon_image)
                startOf(R.id.created_at_text)
                marginEnd = dimen(R.dimen.margin_text_small)
            }

            textView {
                createdAtText = this
                id = R.id.created_at_text
                lines = 1
            }.lparams(wrapContent, wrapContent) {
                bottomOf(R.id.twitter_bird_image)
                alignParentEnd()
            }

            textView {
                this@TweetUIComponent.text = this
                id = R.id.text
            }.lparams(wrapContent, wrapContent) {
                below(R.id.icon_image)
                endOf(R.id.icon_image)
            }

            thumbnails()

            textView {
                id = R.id.via_text
                text = context.getString(R.string.via)
            }.lparams(wrapContent, wrapContent) {
                endOf(R.id.icon_image)
                below(R.id.thumbnails_relative)
                topMargin = dimen(R.dimen.margin_text_small)
                marginEnd = dimen(R.dimen.margin_text_small)
            }

            textView {
                sourceText = this
                id = R.id.source_text
            }.lparams(wrapContent, wrapContent) {
                below(R.id.thumbnails_relative)
                endOf(R.id.via_text)
                topMargin = dimen(R.dimen.margin_text_small)
            }
        }
    }

    private fun _RelativeLayout.thumbnails() {
        fun _RelativeLayout.thumb(id: Int, init: RelativeLayout.LayoutParams.() -> Unit = {}) {
            val thumb = imageView {
                this.id = id
                backgroundColor = context.getColor(R.color.transparent)
            }.lparams(dimen(R.dimen.image_icon), dimen(R.dimen.image_icon), init)
            mThumbnails.add(thumb)
        }

        relativeLayout {
            id = R.id.thumbnails_relative
            lparams(matchParent, matchParent) {
                endOf(R.id.icon_image)
                topMargin = dimen(R.dimen.margin_small)
                below(R.id.text)
            }

            thumb(R.id.thumb_image1)
            thumb(R.id.thumb_image2) {
                endOf(R.id.thumb_image1)
                marginStart = dimen(R.dimen.margin_small)
            }
            thumb(R.id.thumb_image3) {
                endOf(R.id.thumb_image2)
                marginStart = dimen(R.dimen.margin_small)
            }
            thumb(R.id.thumb_image4) {
                endOf(R.id.thumb_image3)
                marginStart = dimen(R.dimen.margin_small)
            }
        }
    }
}