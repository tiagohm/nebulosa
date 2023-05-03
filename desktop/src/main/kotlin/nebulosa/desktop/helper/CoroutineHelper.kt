package nebulosa.desktop.helper

import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okio.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend inline fun <T> withDefault(noinline block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Default, block)
}

suspend inline fun <T> withMain(noinline block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.Main, block)
}

suspend inline fun <T> withIO(noinline block: suspend CoroutineScope.() -> T): T {
    return withContext(Dispatchers.IO, block)
}

@Suppress("NOTHING_TO_INLINE")
inline fun runBlockingDefault(noinline block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.Default, block)
}

@Suppress("NOTHING_TO_INLINE")
inline fun runBlockingMain(noinline block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.Main, block)
}

@Suppress("NOTHING_TO_INLINE")
inline fun runBlockingIO(noinline block: suspend CoroutineScope.() -> Unit) {
    runBlocking(Dispatchers.IO, block)
}

suspend fun Call.await() = suspendCancellableCoroutine { continuation ->
    continuation.invokeOnCancellation {
        cancel()
    }

    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }
    })
}
