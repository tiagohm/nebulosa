package nebulosa.api.tasks

import nebulosa.common.Resettable
import nebulosa.common.concurrency.cancel.CancellationToken
import java.io.Closeable

interface Task : Resettable, Closeable {

    fun execute(cancellationToken: CancellationToken = CancellationToken.NONE)

    override fun reset() = Unit

    override fun close() = Unit

    companion object {

        @JvmStatic
        fun of(vararg tasks: Task) = object : Task {

            override fun execute(cancellationToken: CancellationToken) = tasks.forEach { it.execute(cancellationToken) }

            override fun reset() = tasks.forEach { it.reset() }

            override fun close() = tasks.forEach { it.close() }
        }
    }
}
