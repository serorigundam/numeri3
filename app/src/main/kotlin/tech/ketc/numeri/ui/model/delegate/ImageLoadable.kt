package tech.ketc.numeri.ui.model.delegate

import kotlinx.coroutines.experimental.async
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.util.arch.response.response

class ImageLoadable(private val mImageRepository: IImageRepository) : IImageLoadable {
    override fun imageLoad(urlStr: String, cache: Boolean) = async {
        response { mImageRepository.downloadOrGet(urlStr, cache) }
    }
}