package tech.ketc.numeri.ui.view.media

import android.content.Context
import android.view.ViewManager
import com.github.chrisbanes.photoview.PhotoView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.photoView(theme: Int = 0, init: PhotoView.() -> Unit): PhotoView {
    return ankoView(::PhotoView, theme, init)
}

inline fun Context.photoView(theme: Int = 0, init: PhotoView.() -> Unit): PhotoView {
    return ankoView(::PhotoView, theme, init)
}

