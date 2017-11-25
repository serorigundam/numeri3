package tech.ketc.numeri.util.anko

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoException

interface UIComponent<out Root : View> {
    val componentRoot: Root
    val componentId: Int
        get() {
            if (componentRoot.id == View.NO_ID) componentRoot.id = View.generateViewId()
            return componentRoot.id
        }

    fun createView(ctx: Context): Root
}


fun <T : View> ViewManager.component(uiComponent: UIComponent<T>, init: T.() -> Unit = {}) = when (this) {
    is ViewGroup -> uiComponent.createView(context).also { addView(it.also(init)) }
    is AnkoContext<*> -> uiComponent.createView(ctx).also { addView(it.also(init), null) }
    else -> throw AnkoException("$this is the wrong parent")
}