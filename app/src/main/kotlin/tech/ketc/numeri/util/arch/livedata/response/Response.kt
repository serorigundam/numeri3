package tech.ketc.numeri.util.arch.livedata.response

interface Response<out T> {
    val result: T
    val error: Throwable
    val isSuccessful: Boolean

    fun ifPresent(take: (T) -> Unit) {
        if (isSuccessful) {
            take(result)
        }
    }

    fun ifError(func: (Throwable) -> Unit) {
        if (!isSuccessful) {
            func(error)
        }
    }
}