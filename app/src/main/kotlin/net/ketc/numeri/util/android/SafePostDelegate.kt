package net.ketc.numeri.util.android

import java.util.*

class SafePostDelegate {
    private val taskQueue = ArrayList<() -> Unit>()
    private var pause: Boolean = false
    private var destroyed: Boolean = false


    /**
     * post tasks safely
     */
    fun safePost(task: () -> Unit) {
        if (pause || destroyed) {
            taskQueue.add(task)
        } else {
            task()
        }
    }

    fun onPause() {
        pause = true
    }

    fun onResume() {
        pause = false
        destroyed = false
        if (taskQueue.isNotEmpty()) {
            taskQueue.forEach { it() }
            taskQueue.clear()
        }
    }

    fun onDestroy() {
        destroyed = true
    }
}