package tech.ketc.numeri.util

interface Updatable {
    fun update(complete: () -> Unit = {})
}