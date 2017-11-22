package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.util.arch.coroutine.ResponseDeferred

interface IImageLoadable {
    fun imageLoad(urlStr: String, cache: Boolean = true): ResponseDeferred<BitmapContent>
}
