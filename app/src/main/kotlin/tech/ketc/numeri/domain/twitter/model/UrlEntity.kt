package tech.ketc.numeri.domain.twitter.model

data class UrlEntity(override val displayUrl: String, override val expandUrl: String,
                     override val start: Int, override val end: Int) : IUrlEntity {
    constructor(urlEntity: twitter4j.URLEntity) : this(urlEntity.displayURL, urlEntity.expandedURL, urlEntity.start, urlEntity.end)
}