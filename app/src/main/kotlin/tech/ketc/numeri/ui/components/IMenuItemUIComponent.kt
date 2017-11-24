package tech.ketc.numeri.ui.components

import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import tech.ketc.numeri.util.anko.UIComponent

interface IMenuItemUIComponent : UIComponent<RelativeLayout> {
    val imageView: ImageView
    val textView: TextView
}