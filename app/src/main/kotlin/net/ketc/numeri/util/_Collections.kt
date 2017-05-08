package net.ketc.numeri.util

import java.util.*
import kotlin.coroutines.experimental.buildSequence

fun <T> Collection<T>.copy(): Collection<T> = ArrayList(this)

fun <T> List<T>.copy(): List<T> = ArrayList(this)

fun <T> Collection<T>.toImmutableCollection(): Collection<T> {
    return Collections.unmodifiableCollection(this)
}

fun <T> List<T>.toImmutableList(): List<T> {
    return Collections.unmodifiableList(this)
}

fun <T> Collection<T>.indexesIf(predicate: (T) -> Boolean) = buildSequence {
    this@indexesIf
            .filter { predicate(it) }
            .forEach { yield(indexOf(it)) }
}.toList().toIntArray()