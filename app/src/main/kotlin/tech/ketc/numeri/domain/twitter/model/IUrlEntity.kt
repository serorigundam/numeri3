package tech.ketc.numeri.domain.twitter.model

import java.io.Serializable

interface IUrlEntity : Serializable {
    val displayUrl: String
    val expandUrl: String
    val start: Int
    val end: Int
}