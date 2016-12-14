package net.ketc.numeri.domain.model

data class UrlEntity(val displayUrl: String, val expandUrl: String, val start: Int, val end: Int) {
    constructor(urlEntity: twitter4j.URLEntity) : this(urlEntity.displayURL, urlEntity.expandedURL, urlEntity.start, urlEntity.end)
}