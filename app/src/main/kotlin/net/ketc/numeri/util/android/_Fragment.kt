package net.ketc.numeri.util.android

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

val Fragment.parent: AppCompatActivity
    get() = activity as AppCompatActivity? ?: parentFragment?.parent ?: throw IllegalStateException("there is no instance of Fragment")