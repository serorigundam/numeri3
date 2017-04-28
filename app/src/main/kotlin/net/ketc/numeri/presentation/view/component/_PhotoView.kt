package net.ketc.numeri.presentation.view.component

import android.view.ViewManager
import com.github.chrisbanes.photoview.PhotoView
import org.jetbrains.anko.custom.ankoView

inline fun ViewManager.photoView(theme: Int = 0, init: PhotoView.() -> Unit): PhotoView {
    return ankoView(::PhotoView, theme) { init() }
}