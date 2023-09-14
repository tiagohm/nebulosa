package nebulosa.common.concurrency

import java.util.concurrent.ThreadFactory

object DaemonThreadFactory : ThreadFactory {

    override fun newThread(task: Runnable): Thread {
        val thread = Thread(task)
        thread.isDaemon = true
        return thread
    }
}
