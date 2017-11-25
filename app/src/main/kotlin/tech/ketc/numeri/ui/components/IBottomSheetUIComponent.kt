package tech.ketc.numeri.ui.components

import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import tech.ketc.numeri.util.anko.UIComponent

interface IBottomSheetUIComponent : UIComponent<RelativeLayout> {
    val messageTextView: TextView
    val content: ViewGroup
}