package tech.ketc.numeri.ui.model.delegate

import kotlinx.coroutines.experimental.Deferred
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.util.arch.response.Response

interface IImageLoadable {
    fun imageLoad(urlStr: String, cache: Boolean = true)
            : Deferred<Response<BitmapContent>>
}
