package tech.ketc.numeri.ui.components

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import org.jetbrains.anko.image

fun createMenuItemUIComponent(ctx: Context, @DrawableRes icon: Int, text: String) = MenuItemUIComponent().apply {
    createView(ctx)
    imageView.image = ctx.getDrawable(icon)
    textView.text = text
}

fun createMenuItemUIComponent(ctx: Context, @DrawableRes icon: Int, @StringRes id: Int)
        = createMenuItemUIComponent(ctx, icon, ctx.getString(id))