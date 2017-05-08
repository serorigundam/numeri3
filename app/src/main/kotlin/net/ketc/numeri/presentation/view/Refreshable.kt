package net.ketc.numeri.presentation.view

interface Refreshable {
    fun refresh(callback: () -> Unit)
}