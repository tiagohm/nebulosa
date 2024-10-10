package nebulosa.util.concurrency

import java.util.concurrent.ThreadFactory

data object DaemonThreadFactory : ThreadFactory {

    override fun newThread(task: Runnable): Thread {
        val thread = Thread(task)
        thread.isDaemon = true
        return thread
    }
}
