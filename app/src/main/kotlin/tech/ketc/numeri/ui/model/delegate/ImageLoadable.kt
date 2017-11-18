package tech.ketc.numeri.ui.model.delegate

import android.arch.lifecycle.LifecycleOwner
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.util.arch.BindingLifecycleAsyncTask
import tech.ketc.numeri.util.arch.response.Response

class ImageLoadable(private val mImageRepository: IImageRepository) : IImageLoadable {
    override fun imageLoad(owner: LifecycleOwner, urlStr: String, cache: Boolean, handle: (Response<BitmapContent>) -> Unit)
            = BindingLifecycleAsyncTask { mImageRepository.downloadOrGet(urlStr, cache) }.also { it.run(owner, handle) }
}