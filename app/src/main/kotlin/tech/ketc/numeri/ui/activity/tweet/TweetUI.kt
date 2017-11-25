package tech.ketc.numeri.ui.activity.tweet

import android.support.v7.widget.Toolbar
import android.text.InputType
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.*
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.support.v4.nestedScrollView
import tech.ketc.numeri.R
import tech.ketc.numeri.util.anko.*
import tech.ketc.numeri.util.unmodifiableList

class TweetUI : ITweetUI {
    override lateinit var toolbar: Toolbar
        private set
    override lateinit var editText: EditText
        private set
    override lateinit var tweetSendButton: Button
        private set
    override lateinit var cameraButton: ImageButton
        private set
    override lateinit var selectMediaButton: ImageButton
        private set
    override lateinit var remainingText: TextView
        private set
    override lateinit var replyInfoText: TextView
        private set
    override lateinit var backgroundStreamText: TextView
        private set
    override lateinit var thumbnailsFrame: FrameLayout
        private set
    override val thumbnails: List<ImageView>
        get() = mThumbnails.unmodifiableList()
    override lateinit var userSelectButton: ImageButton
        private set

    private val mThumbnails = ArrayList<ImageView>()

    override fun createView(ui: AnkoContext<TweetActivity>) = ui.create {
        relativeLayout {
            appBarLayout {
                id = R.id.appbar
                toolbar {
                    toolbar = this
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, wrapContent)

            nestedScrollView {
                relativeLayout {
                    textView {
                        backgroundStreamText = this
                        id = R.id.background_stream_text
                        lines = 1
                        maxLines = 1
                        textColor = color(resourceId(android.R.attr.textColorSecondary))
                    }.lparams(wrapContent, wrapContent) {
                        alignParentTop()
                        alignParentStart()
                        marginBottom = dimen(R.dimen.margin_text_small)
                        marginEnd = dimen(R.dimen.margin_text_small)
                        marginStart = dimen(R.dimen.margin_text_small)
                    }

                    textInputLayout {
                        id = R.id.text_input
                        editText {
                            this@TweetUI.editText = this
                            inputType = InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
                            singleLine = false
                            hint = context.getString(R.string.tweet_input)
                            gravity = Gravity.TOP or Gravity.START
                        }.textInputlparams(matchParent, wrapContent)
                    }.lparams(matchParent, matchParent) {
                        below(R.id.background_stream_text)
                        marginBottom = dimen(R.dimen.margin_small)
                    }

                    frameLayout {
                        thumbnailsFrame = this
                        id = R.id.thumbnails_frame
                        visibility = View.GONE
                        horizontalScrollView {
                            relativeLayout {
                                fun thumb(idRes: Int, init: RelativeLayout.LayoutParams.() -> Unit) {
                                    imageView {
                                        id = idRes
                                        mThumbnails.add(this)
                                    }.lparams(dip(72), dip(72), init)
                                }

                                thumb(R.id.thumb_image1) {
                                    alignParentStart()
                                    marginEnd = dimen(R.dimen.margin_small)
                                }

                                thumb(R.id.thumb_image2) {
                                    endOf(R.id.thumb_image1)
                                    marginEnd = dimen(R.dimen.margin_small)
                                }

                                thumb(R.id.thumb_image3) {
                                    endOf(R.id.thumb_image2)
                                    marginEnd = dimen(R.dimen.margin_small)
                                }

                                thumb(R.id.thumb_image4) {
                                    endOf(R.id.thumb_image3)
                                    marginEnd = dimen(R.dimen.margin_small)
                                }
                            }
                        }
                    }.lparams(matchParent, wrapContent) {
                        below(R.id.text_input)
                        marginBottom = dimen(R.dimen.margin_small)
                    }

                    textView {
                        replyInfoText = this
                        lines = 1
                        minLines = 0
                        ellipsize = TextUtils.TruncateAt.END
                        textColor = color(resourceId(android.R.attr.textColorSecondary))
                    }.lparams(matchParent, wrapContent) {
                        below(R.id.thumbnails_frame)
                        marginEnd = dimen(R.dimen.margin_text_small)
                        marginStart = dimen(R.dimen.margin_text_small)
                    }
                }.lparams(matchParent, wrapContent) {
                    margin = dimen(R.dimen.margin_medium)
                }
            }.lparams(matchParent, matchParent) {
                below(R.id.appbar)
                topOf(R.id.bottom_content)
            }

            frameLayout {
                id = R.id.bottom_content
                backgroundColor = color(R.color.colorPrimaryDark)

                relativeLayout {
                    horizontalScrollView {
                        relativeLayout {
                            imageButton {
                                cameraButton = this
                                id = R.id.camera_button
                                image = drawable(R.drawable.ic_photo_camera_white_24dp)
                                background = drawable(R.drawable.ripple_corner_transparent)
                            }.lparams(dip(40), wrapContent) {
                                marginEnd = dimen(R.dimen.margin_small)
                                alignParentStart()
                                centerVertically()
                            }

                            imageButton {
                                selectMediaButton = this
                                image = drawable(R.drawable.ic_image_white_24dp)
                                background = drawable(R.drawable.ripple_corner_transparent)
                            }.lparams(dip(40), wrapContent) {
                                endOf(R.id.camera_button)
                                centerVertically()
                            }
                        }
                    }.lparams(wrapContent, matchParent) {
                        startOf(R.id.user_select_button)
                        alignParentStart()
                        centerVertically()
                    }

                    imageButton {
                        userSelectButton = this
                        id = R.id.user_select_button
                        backgroundColor = color(R.color.image_background)
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }.lparams(dip(40), matchParent) {
                        startOf(R.id.remaining_text)
                        marginEnd = dimen(R.dimen.margin_small)
                    }

                    textView {
                        remainingText = this
                        id = R.id.remaining_text
                        gravity = Gravity.CENTER
                        setEms(2)
                    }.lparams(wrapContent, wrapContent) {
                        startOf(R.id.tweet_send_button)
                        marginEnd = dimen(R.dimen.margin_small)
                    }

                    button {
                        id = R.id.tweet_send_button
                        tweetSendButton = this
                        isEnabled = false
                        background = drawable(R.drawable.ripple_button_background)
                        text = string(R.string.label_tweet)
                        textColor = color(resourceId(android.R.attr.textColorPrimary))
                    }.lparams(dip(72), matchParent) {
                        alignParentEnd()
                        centerVertically()
                    }
                }.lparams(matchParent, matchParent) {
                    marginTop = dimen(R.dimen.margin_small)
                    marginBottom = dimen(R.dimen.margin_small)
                    marginEnd = dimen(R.dimen.margin_medium)
                    marginStart = dimen(R.dimen.margin_medium)
                }
            }.lparams(matchParent, dip(56)) {
                alignParentBottom()
                alignParentStart()
            }
        }
    }
}