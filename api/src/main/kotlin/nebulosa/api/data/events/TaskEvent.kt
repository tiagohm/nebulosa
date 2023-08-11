package nebulosa.api.data.events

import nebulosa.common.concurrency.ThreadedJob

sealed interface TaskEvent {

    val task: ThreadedJob<*>
}
