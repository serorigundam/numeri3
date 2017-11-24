package tech.ketc.numeri.util

import java.util.*

fun <T> Collection<T>.unmodifiableCollection(): Collection<T> = Collections.unmodifiableCollection(this)

fun <T> List<T>.unmodifiableList(): List<T> = Collections.unmodifiableList(this)

fun <T> Set<T>.unmodifiableSet(): Set<T> = Collections.unmodifiableSet(this)

fun <K, V> Map<K, V>.unmodifiableMap(): Map<K, V> = Collections.unmodifiableMap(this)

fun <T> List<T>.copy() = map { it }