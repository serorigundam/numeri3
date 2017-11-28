package tech.ketc.numeri.ui.model

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.ArrayMap
import tech.ketc.numeri.domain.model.BitmapContent
import tech.ketc.numeri.domain.repository.IImageRepository
import tech.ketc.numeri.ui.model.delegate.IImageLoadable
import tech.ketc.numeri.ui.model.delegate.ImageLoadable
import tech.ketc.numeri.util.Logger
import tech.ketc.numeri.util.arch.livedata.mediate
import tech.ketc.numeri.util.arch.livedata.observeNonnullOnly
import tech.ketc.numeri.util.logTag
import javax.inject.Inject

class MediaViewModel @Inject constructor(imageRepository: IImageRepository)
    : ViewModel(), IImageLoadable by ImageLoadable(imageRepository) {
    private val mLiveDataMap = HashMap<String, MutableLiveData<BitmapContent>>()
    private val mErrorMap = HashMap<String, Boolean>()
    private fun liveData(url: String) = mLiveDataMap.getOrPut(url) {
        Logger.v(logTag, "put new Livedata $url")
        MutableLiveData()
    }

    fun putBitmapContent(url: String, content: BitmapContent) {
        Logger.v(logTag, "put bitmap content $content")
        liveData(url).value = content
    }

    fun putError(url: String) {
        mErrorMap.getOrPut(url) { true }
    }

    fun isError(url: String): Boolean {
        return mErrorMap[url] ?: false
    }

    fun observeBitmapContent(owner: LifecycleOwner, url: String, handle: (BitmapContent) -> Unit) {
        val value = liveData(url).value
        Logger.v(logTag, "$value")
        if (value != null) handle(value)
        else liveData(url).mediate().observeNonnullOnly(owner, handle)
    }
}