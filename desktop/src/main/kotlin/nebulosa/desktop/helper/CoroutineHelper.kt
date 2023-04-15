package nebulosa.desktop.helper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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
