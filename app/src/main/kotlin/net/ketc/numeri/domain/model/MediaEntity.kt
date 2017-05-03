package net.ketc.numeri.domain.model

import twitter4j.MediaEntity
import java.io.Serializable

data class MediaEntity(val url: String, val type: MediaType, val variants: List<Variant>) : Serializable {

    init {
        if (variants !is Serializable) {
            throw IllegalArgumentException("variants must be a serializable List")
        }
    }

    constructor(entity: MediaEntity) : this(entity.mediaURL,
            entity.type.toType(),
            entity.videoVariants.orEmpty().map(::Variant))
}

private fun String.toType() = MediaType.values().find { it.typeStr == this }!!
data class Variant(val bitrate: Int, val contentType: String, val url: String) : Serializable {
    constructor(variant: twitter4j.MediaEntity.Variant) : this(variant.bitrate, variant.contentType, variant.url)
}

enum class MediaType(val typeStr: String) : Serializable {
    PHOTO("photo"), VIDEO("video"), ANIMATED_GIF("animated_gif")
}