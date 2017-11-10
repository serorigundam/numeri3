package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable

enum class MediaType(val typeStr: String) : Serializable {
    PHOTO("photo"), VIDEO("video"), ANIMATED_GIF("animated_gif")
}