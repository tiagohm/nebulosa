package nebulosa.common.concurrency

import java.io.Closeable

interface Worker : Runnable, Closeable {

    val stopped: Boolean
}
