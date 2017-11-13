package tech.ketc.numeri.ui.components

import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import tech.ketc.numeri.util.anko.UIComponent

interface IAccountUIComponent : UIComponent<RelativeLayout> {
    val screenNameText: TextView
    val userNameText: TextView
    val iconImage: ImageView
}
