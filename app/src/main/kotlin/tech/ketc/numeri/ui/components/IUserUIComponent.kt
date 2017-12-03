package tech.ketc.numeri.ui.components

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import tech.ketc.numeri.util.anko.UIComponent

interface IUserUIComponent : UIComponent<RelativeLayout> {
    val screenNameText: TextView
    val userNameText: TextView
    val descriptionText: TextView
    val followButton: ImageButton
    val iconImage: ImageView
    val iconBack: View
}