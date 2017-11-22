package tech.ketc.numeri.ui.model.delegate

import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.util.arch.coroutine.asyncResponse

class ImageLoadable(private val mImageRepository: IImageRepository) : IImageLoadable {
    override fun imageLoad(urlStr: String, cache: Boolean) = asyncResponse {
        mImageRepository.downloadOrGet(urlStr, cache)
    }
}