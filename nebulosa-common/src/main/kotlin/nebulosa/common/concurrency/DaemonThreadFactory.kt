package nebulosa.common.concurrency

import java.util.concurrent.ThreadFactory

object DaemonThreadFactory : ThreadFactory {

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r)
        thread.isDaemon = true
        return thread
    }
}
