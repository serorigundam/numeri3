package tech.ketc.numeri.util.arch.response

interface Response<out T : Any> {
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