package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.util.arch.response.Response

interface IImageLoadable {
    fun imageLoad(owner: LifecycleOwner, urlStr: String, handle: (Response<BitmapContent>) -> Unit)
}
