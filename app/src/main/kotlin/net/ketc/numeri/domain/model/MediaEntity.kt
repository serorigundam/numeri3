package net.ketc.numeri.domain.model

import twitter4j.ExtendedMediaEntity

data class MediaEntity(val url: String, val type: MediaType, val variants: List<Variant>) {
    constructor(entity: ExtendedMediaEntity) : this(entity.expandedURL,
            entity.type.totType(),
            entity.videoVariants.orEmpty().map(::Variant))
}

private fun String.totType() = MediaType.values().find { it.typeStr == this }!!
data class Variant(val bitrate: Int, val contentType: String, val url: String) {
    constructor(variant: twitter4j.ExtendedMediaEntity.Variant) : this(variant.bitrate, variant.contentType, variant.url)
}

enum class MediaType(val typeStr: String) {
    PHOTO("photo"), VIDEO("video"), ANIMATED_GIF("animated_gif")
}