package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.IImageRepository
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.response.Response

class ImageLoadable(private val imageRepository: IImageRepository) : IImageLoadable {
    override fun imageLoad(owner: LifecycleOwner, urlStr: String, handle: (Response<BitmapContent>) -> Unit)
            = BindingLifecycleAsyncTask { imageRepository.downloadOrGet(urlStr) }.run(owner, handle)
}