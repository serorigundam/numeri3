package tech.ketc.numeri.domain.twitter.model

import twitter4j.MediaEntity
import java.io.Serializable

data class MediaEntity(val url: String, val type: MediaType, val variants: List<Variant>) : Serializable {

    constructor(entity: MediaEntity) : this(entity.mediaURL,
            MediaType.values().find { it.typeStr == entity.type } ?: throw IllegalArgumentException(),
            entity.videoVariants.orEmpty().map(::Variant).sortedBy { it.bitrate })
}