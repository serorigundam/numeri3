package tech.ketc.numeri.domain.repository

import tech.ketc.numeri.domain.model.BitmapContent

interface IImageRepository {
    fun downloadOrGet(urlStr: String, cache: Boolean = true): BitmapContent
}