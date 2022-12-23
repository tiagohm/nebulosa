package nebulosa.server

import java.time.LocalDateTime

interface ThreadedTask : Runnable {

    val startedAt: LocalDateTime

    val finishedAt: LocalDateTime

    fun finishGracefully()
}
