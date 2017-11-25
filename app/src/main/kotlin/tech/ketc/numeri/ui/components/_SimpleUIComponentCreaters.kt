package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.view.View
import org.jetbrains.anko.image

fun createMenuItemUIComponent(ctx: Context, @DrawableRes icon: Int, text: String) = MenuItemUIComponent().apply {
    createView(ctx)
    imageView.image = ctx.getDrawable(icon)
    textView.text = text
}

fun createMenuItemUIComponent(ctx: Context, @DrawableRes icon: Int, @StringRes id: Int)
        = createMenuItemUIComponent(ctx, icon, ctx.getString(id))

fun createBottomSheetUIComponent(ctx: Context, message: String, vararg contentChild: View = emptyArray()) = BottomSheetUIComponent().apply {
    createView(ctx)
    messageTextView.text = message
    contentChild.forEach(content::addView)
}

fun createBottomSheetUIComponent(ctx: Context, @StringRes id: Int, vararg contentChild: View = emptyArray())
        = createBottomSheetUIComponent(ctx, ctx.getString(id), *contentChild)