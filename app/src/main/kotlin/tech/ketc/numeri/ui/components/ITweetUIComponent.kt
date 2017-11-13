package tech.ketc.numeri.ui.components

import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import tech.ketc.numeri.util.anko.UIComponent

interface ITweetUIComponent : UIComponent<FrameLayout> {
    val iconImage: ImageView
    val subInfoIcon: ImageView
    val subInfoText: TextView
    val screenNameText: TextView
    val userNameText: TextView
    val createdAtText: TextView
    val text: TextView
    val thumbnails: List<ImageView>
    val sourceText: TextView
    val overlayRelative: RelativeLayout
}