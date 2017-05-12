package net.ketc.numeri.domain.model

import java.io.Serializable

data class UrlEntity(val displayUrl: String, val expandUrl: String, val start: Int, val end: Int):Serializable {
    constructor(urlEntity: twitter4j.URLEntity) : this(urlEntity.displayURL, urlEntity.expandedURL, urlEntity.start, urlEntity.end)
}