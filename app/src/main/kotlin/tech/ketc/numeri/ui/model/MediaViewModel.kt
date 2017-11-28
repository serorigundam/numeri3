package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.util.arch.livedata.observeNonnullOnly
import javax.inject.Inject

class MediaViewModel @Inject constructor(imageRepository: IImageRepository)
    : ViewModel(), IImageLoadable by ImageLoadable(imageRepository) {
    private val mLiveDataMap = HashMap<String, MutableLiveData<BitmapContent>>()

    fun putBitmapContent(url: String, content: BitmapContent) {
        mLiveDataMap.getOrPut(url) {
            MutableLiveData<BitmapContent>().apply {
                postValue(content)
            }
        }
    }

    fun observeBitmapContent(owner: LifecycleOwner, url: String, handle: (BitmapContent) -> Unit) {
        mLiveDataMap[url]!!.observeNonnullOnly(owner, handle)
    }
}