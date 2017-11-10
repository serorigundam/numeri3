package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable

data class Variant(val bitrate: Int, val contentType: String, val url: String) : Serializable {
    constructor(variant: twitter4j.MediaEntity.Variant) : this(variant.bitrate, variant.contentType, variant.url)
}