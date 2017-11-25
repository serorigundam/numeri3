package tech.ketc.numeri.ui.activity.tweet

import android.support.v7.widget.Toolbar
import android.widget.*
import org.jetbrains.anko.AnkoComponent

interface ITweetUI : AnkoComponent<TweetActivity> {
    val toolbar: Toolbar
    val editText: EditText
    val tweetSendButton: Button
    val cameraButton: ImageButton
    val selectMediaButton: ImageButton
    val remainingText: TextView
    val replyInfoText: TextView
    val backgroundStreamText: TextView
    val thumbnailsFrame: FrameLayout
    val thumbnails: List<ImageView>
    val userSelectButton: ImageButton
}