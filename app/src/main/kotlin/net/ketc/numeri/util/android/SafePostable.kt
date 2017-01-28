package net.ketc.numeri.util.android

import java.util.*

class SafePostDelegate {
    private val taskQueue = ArrayList<() -> Unit>()
    private var pause: Boolean = false

    /**
     * post tasks safely
     */
    fun safePost(task: () -> Unit) {
        if (pause) {
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
        taskQueue.forEach { it() }
        taskQueue.clear()
    }
}