package nebulosa.api.tasks

import nebulosa.common.Resettable
import nebulosa.common.concurrency.cancel.CancellationToken
import java.io.Closeable

interface Task : Resettable, Closeable {

    fun execute(cancellationToken: CancellationToken = CancellationToken.NONE)

    override fun reset() = Unit

    override fun close() = Unit
}
